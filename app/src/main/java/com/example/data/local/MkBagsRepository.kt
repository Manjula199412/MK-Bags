package com.example.data.local

import androidx.room.withTransaction
import com.example.data.model.CartItem
import com.example.data.model.CartItemWithProduct
import com.example.data.model.CustomerOrder
import com.example.data.model.DealAlert
import com.example.data.model.OrderLineItem
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMode
import com.example.data.model.Product
import com.example.data.remote.OrderSyncTransport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.roundToInt
import kotlin.random.Random

class MkBagsRepository(
    private val database: MkBagsDatabase,
    private val syncTransport: OrderSyncTransport? = null
) {
    private val productDao = database.productDao()
    private val cartDao = database.cartDao()
    private val orderDao = database.orderDao()
    private val dealAlertDao = database.dealAlertDao()
    private val syncMutex = Mutex()

    val allProducts: Flow<List<Product>> = productDao.observeAllProducts()
    val activeProducts: Flow<List<Product>> = productDao.observeActiveProducts()
    val allOrders: Flow<List<CustomerOrder>> = orderDao.observeAllOrders()
    val customerOrders: Flow<List<CustomerOrder>> = orderDao.observeCustomerOrders()
    val dealAlerts: Flow<List<DealAlert>> = dealAlertDao.observeDealAlerts()

    val cartItemsWithProducts: Flow<List<CartItemWithProduct>> = combine(
        cartDao.observeCartItems(),
        productDao.observeAllProducts()
    ) { cartItems, products ->
        val productMap = products.associateBy { it.id }
        cartItems.mapNotNull { item ->
            productMap[item.productId]?.let { product ->
                CartItemWithProduct(product = product, quantity = item.quantity)
            }
        }
    }

    suspend fun addToCart(product: Product, addQuantity: Int = 1): Boolean {
        if (product.stock <= 0) return false
        val existing = cartDao.getCartItem(product.id)
        val newQty = ((existing?.quantity ?: 0) + addQuantity).coerceAtMost(product.stock)
        cartDao.upsertCartItem(CartItem(productId = product.id, quantity = newQty))
        return true
    }

    suspend fun updateCartQuantity(productId: Long, quantity: Int, maxStock: Int) {
        if (quantity <= 0) {
            cartDao.removeCartItem(productId)
        } else {
            cartDao.upsertCartItem(
                CartItem(productId = productId, quantity = quantity.coerceAtMost(maxStock.coerceAtLeast(1)))
            )
        }
    }

    suspend fun removeFromCart(productId: Long) {
        cartDao.removeCartItem(productId)
    }

    suspend fun saveProduct(product: Product): Long {
        return if (product.id == 0L) {
            productDao.insertProduct(product)
        } else {
            productDao.updateProduct(product)
            product.id
        }
    }

    suspend fun adjustProductStock(productId: Long, delta: Int) {
        productDao.adjustStock(productId, delta)
    }

    suspend fun deleteProduct(productId: Long) {
        cartDao.removeCartItem(productId)
        productDao.deleteProductById(productId)
    }

    suspend fun placeOrder(
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        cartItems: List<CartItemWithProduct>,
        paymentMode: PaymentMode,
        codUpfrontPercent: Int,
        upiTransactionRef: String,
        paymentScreenshotKey: String = ""
    ): Result<CustomerOrder> {
        if (cartItems.isEmpty()) {
            return Result.failure(IllegalStateException("Your cart is empty."))
        }
        return try {
            val placedOrder = database.withTransaction {
                val lineItems = cartItems.map {
                    OrderLineItem(
                        productId = it.product.id,
                        name = it.product.name,
                        quantity = it.quantity,
                        unitPriceInr = it.product.priceInr
                    )
                }
                val totalAmount = lineItems.sumOf { it.lineTotalInr }
                val upfront = when (paymentMode) {
                    PaymentMode.UPI_FULL -> totalAmount
                    PaymentMode.COD_65_UPFRONT ->
                        ((totalAmount * (codUpfrontPercent.coerceIn(10, 100) / 100.0))).roundToInt().toDouble()
                }
                val balanceDue = (totalAmount - upfront).coerceAtLeast(0.0)
                val now = System.currentTimeMillis()
                val uniqueCode = "${Random.nextInt(100, 999)}${(now % 10000).toString().padStart(4, '0')}"
                val orderNumber = "MKB-$uniqueCode"
                val order = CustomerOrder(
                    orderNumber = orderNumber,
                    customerName = customerName.trim(),
                    customerPhone = customerPhone.trim(),
                    deliveryAddress = deliveryAddress.trim(),
                    itemsJson = CustomerOrder.encodeLineItems(lineItems),
                    totalAmountInr = totalAmount,
                    paymentMode = paymentMode.name,
                    upfrontPaidInr = upfront,
                    balanceDueOnDeliveryInr = balanceDue,
                    upiTransactionRef = upiTransactionRef.trim(),
                    paymentScreenshotKey = paymentScreenshotKey,
                    status = OrderStatus.PENDING_CONFIRMATION.name,
                    adminNote = "Order placed. Awaiting payment verification.",
                    isLocalCustomerOrder = true,
                    pendingSync = true,
                    createdAt = now,
                    updatedAt = now
                )
                val orderId = orderDao.insertOrder(order)
                cartItems.forEach { item ->
                    productDao.decrementStock(item.product.id, item.quantity)
                }
                cartDao.clearCart()
                order.copy(id = orderId)
            }
            Result.success(placedOrder)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun attachScreenshotToOrder(orderId: Long, screenshotKey: String) {
        orderDao.updateOrderPaymentScreenshot(
            orderId = orderId,
            screenshotKey = screenshotKey,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun attachScreenshotToRecentOrder(screenshotKey: String): CustomerOrder? {
        val recent = orderDao.getMostRecentCustomerOrder()
            ?: orderDao.getMostRecentOrder()
            ?: return null
        val now = System.currentTimeMillis()
        orderDao.updateOrderPaymentScreenshot(
            orderId = recent.id,
            screenshotKey = screenshotKey,
            updatedAt = now
        )
        return recent.copy(paymentScreenshotKey = screenshotKey, pendingSync = true, updatedAt = now)
    }

    suspend fun updateOrderStatus(orderId: Long, status: OrderStatus, adminNote: String) {
        val defaultNote = adminNote.ifBlank { status.description }
        orderDao.updateOrderStatus(
            orderId = orderId,
            status = status.name,
            adminNote = defaultNote,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun syncOrders(includeAllRemoteOrders: Boolean): Result<Int> {
        val transport = syncTransport ?: return Result.success(0)
        if (!transport.isConfigured) return Result.success(0)

        return syncMutex.withLock {
            try {
                // 1. Push any pending local orders or local status/screenshot updates first
                val pending = orderDao.getPendingSyncOrders()
                for (order in pending) {
                    val encodedScreenshot = ImageStorageHelper.encodeImageForSync(order.paymentScreenshotKey)
                    val pushResult = transport.pushOrder(order, encodedScreenshot)
                    if (pushResult.isSuccess) {
                        orderDao.markOrderSynced(order.id)
                    }
                }

                // 2. Pull latest orders / status updates from Google Sheets
                val fetchResult = transport.fetchOrders()
                val remoteOrders = fetchResult.getOrElse { err ->
                    return@withLock Result.failure(err)
                }

                var updatedCount = 0
                for (remote in remoteOrders) {
                    val existing = orderDao.getOrderByNumber(remote.orderNumber)
                    if (existing == null) {
                        if (includeAllRemoteOrders) {
                            val localScreenshot = if (remote.screenshotBase64.isNotBlank()) {
                                transport.decodeRemoteScreenshot(remote.orderNumber, remote.screenshotBase64).orEmpty()
                            } else ""
                            orderDao.insertOrder(
                                CustomerOrder(
                                    orderNumber = remote.orderNumber,
                                    customerName = remote.customerName,
                                    customerPhone = remote.customerPhone,
                                    deliveryAddress = remote.deliveryAddress,
                                    itemsJson = remote.itemsJson,
                                    totalAmountInr = remote.totalAmountInr,
                                    paymentMode = remote.paymentMode,
                                    upfrontPaidInr = remote.upfrontPaidInr,
                                    balanceDueOnDeliveryInr = remote.balanceDueOnDeliveryInr,
                                    upiTransactionRef = remote.upiTransactionRef,
                                    paymentScreenshotKey = localScreenshot,
                                    status = remote.status,
                                    adminNote = remote.adminNote,
                                    isLocalCustomerOrder = false,
                                    pendingSync = false,
                                    createdAt = remote.createdAt,
                                    updatedAt = remote.updatedAt
                                )
                            )
                            updatedCount++
                        }
                    } else if (!existing.pendingSync && remote.updatedAt > existing.updatedAt) {
                        val updatedScreenshot = if (remote.screenshotBase64.isNotBlank()) {
                            transport.decodeRemoteScreenshot(remote.orderNumber, remote.screenshotBase64)
                                ?: existing.paymentScreenshotKey
                        } else {
                            existing.paymentScreenshotKey
                        }
                        orderDao.updateOrder(
                            existing.copy(
                                status = remote.status,
                                adminNote = remote.adminNote,
                                paymentScreenshotKey = updatedScreenshot,
                                updatedAt = remote.updatedAt,
                                pendingSync = false
                            )
                        )
                        updatedCount++
                    }
                }
                Result.success(updatedCount)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun publishDealAlert(title: String, message: String, promoCode: String): DealAlert {
        val alert = DealAlert(
            title = title.trim(),
            message = message.trim(),
            promoCode = promoCode.trim().uppercase()
        )
        val id = dealAlertDao.insertAlert(alert)
        return alert.copy(id = id)
    }

    suspend fun deleteDealAlert(alert: DealAlert) {
        dealAlertDao.deleteAlert(alert)
    }
}
