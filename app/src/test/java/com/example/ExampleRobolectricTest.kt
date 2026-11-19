package com.example

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.MkBagsDatabase
import com.example.data.local.MkBagsRepository
import com.example.data.model.CustomerOrder
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMode
import com.example.data.model.Product
import com.example.data.remote.OrderSyncTransport
import com.example.data.remote.RemoteOrderRecord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: MkBagsDatabase
    private lateinit var repository: MkBagsRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, MkBagsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = MkBagsRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `read string and load all bundled fonts cleanly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MK Bags", appName)

        listOf(
            R.font.outfit,
            R.font.playfair_display,
            R.font.plus_jakarta_sans,
            R.font.space_grotesk
        ).forEach { fontRes ->
            assertNotNull("Font resource $fontRes failed to load", ResourcesCompat.getFont(context, fontRes))
        }
    }

    @Test
    fun `checkout with COD calculates 65 percent upfront and attaches screenshot to recent order`() = runBlocking {
        val productId = repository.saveProduct(
            Product(
                name = "Test Combo Bag",
                description = "Combo deal",
                category = "Combos",
                priceInr = 1000.0,
                originalPriceInr = 1600.0,
                stock = 5,
                imageKey = "res:img_bag_combo",
                isCombo = true
            )
        )
        val savedProduct = repository.allProducts.first().first { it.id == productId }
        assertTrue(repository.addToCart(savedProduct, 1))

        val cartItems = repository.cartItemsWithProducts.first()
        assertEquals(1, cartItems.size)

        val orderResult = repository.placeOrder(
            customerName = "Asha",
            customerPhone = "9876543210",
            deliveryAddress = "Hyderabad",
            cartItems = cartItems,
            paymentMode = PaymentMode.COD_65_UPFRONT,
            codUpfrontPercent = 65,
            upiTransactionRef = "Screenshot Pending"
        )
        assertTrue(orderResult.isSuccess)
        val order = orderResult.getOrThrow()
        assertEquals(650.0, order.upfrontPaidInr, 0.01)
        assertEquals(350.0, order.balanceDueOnDeliveryInr, 0.01)
        assertEquals(OrderStatus.PENDING_CONFIRMATION, order.orderStatus)
        assertEquals("", order.paymentScreenshotKey)

        val attached = repository.attachScreenshotToRecentOrder("file:/tmp/shared_pay_ss_1.jpg")
        assertNotNull(attached)
        assertEquals(order.id, attached!!.id)

        val reloaded = repository.allOrders.first().first { it.id == order.id }
        assertEquals("file:/tmp/shared_pay_ss_1.jpg", reloaded.paymentScreenshotKey)

        repository.updateOrderStatus(order.id, OrderStatus.CONFIRMED, "Confirmed by admin")
        val updatedOrder = repository.allOrders.first().first { it.id == order.id }
        assertEquals(OrderStatus.CONFIRMED, updatedOrder.orderStatus)
        assertEquals("Confirmed by admin", updatedOrder.adminNote)

        val updatedProduct = repository.allProducts.first().first { it.id == productId }
        assertEquals(4, updatedProduct.stock)
    }

    @Test
    fun `room migrations 1 to 2 and 2 to 3 preserve existing orders cleanly`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "migration_test_mk_bags.db"
        context.deleteDatabase(dbName)

        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                            "CREATE TABLE IF NOT EXISTS `products` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `category` TEXT NOT NULL, `priceInr` REAL NOT NULL, `originalPriceInr` REAL NOT NULL, `stock` INTEGER NOT NULL, `imageKey` TEXT NOT NULL, `isCombo` INTEGER NOT NULL, `isBudgetDeal` INTEGER NOT NULL, `isLimitedOffer` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, `badgeText` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)"
                        )
                        db.execSQL(
                            "CREATE TABLE IF NOT EXISTS `cart_items` (`productId` INTEGER PRIMARY KEY NOT NULL, `quantity` INTEGER NOT NULL, `addedAt` INTEGER NOT NULL)"
                        )
                        db.execSQL(
                            "CREATE TABLE IF NOT EXISTS `orders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `orderNumber` TEXT NOT NULL, `customerName` TEXT NOT NULL, `customerPhone` TEXT NOT NULL, `deliveryAddress` TEXT NOT NULL, `itemsJson` TEXT NOT NULL, `totalAmountInr` REAL NOT NULL, `paymentMode` TEXT NOT NULL, `upfrontPaidInr` REAL NOT NULL, `balanceDueOnDeliveryInr` REAL NOT NULL, `upiTransactionRef` TEXT NOT NULL, `status` TEXT NOT NULL, `adminNote` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)"
                        )
                        db.execSQL(
                            "CREATE TABLE IF NOT EXISTS `deal_alerts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `message` TEXT NOT NULL, `promoCode` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)"
                        )
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                })
                .build()
        )
        helper.writableDatabase.use { sqlDb ->
            sqlDb.execSQL(
                """
                INSERT INTO orders (
                    orderNumber, customerName, customerPhone, deliveryAddress, itemsJson,
                    totalAmountInr, paymentMode, upfrontPaidInr, balanceDueOnDeliveryInr,
                    upiTransactionRef, status, adminNote, createdAt, updatedAt
                ) VALUES (
                    'MKB-1001', 'Kiran', '9988776655', 'Secunderabad', '[]',
                    999.0, 'UPI_FULL', 999.0, 0.0, 'UTR99', 'CONFIRMED', 'Legacy order', 1000, 1000
                )
                """.trimIndent()
            )
        }
        helper.close()

        val migratedDb = Room.databaseBuilder(context, MkBagsDatabase::class.java, dbName)
            .addMigrations(MkBagsDatabase.MIGRATION_1_2, MkBagsDatabase.MIGRATION_2_3)
            .allowMainThreadQueries()
            .build()

        try {
            val orders = migratedDb.orderDao().observeAllOrders().first()
            assertEquals(1, orders.size)
            assertEquals("MKB-1001", orders[0].orderNumber)
            assertEquals("", orders[0].paymentScreenshotKey)
            assertTrue(orders[0].isLocalCustomerOrder)
            assertTrue(orders[0].pendingSync)
        } finally {
            migratedDb.close()
            context.deleteDatabase(dbName)
        }
    }

    @Test
    fun `cross device order sync delivers customer order to admin phone and syncs status back`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val cloudSheet = linkedMapOf(
            "MKB-OTHER-999" to RemoteOrderRecord(
                orderNumber = "MKB-OTHER-999",
                customerName = "Other Customer",
                customerPhone = "9000000000",
                deliveryAddress = "Vijayawada",
                itemsJson = "[]",
                totalAmountInr = 499.0,
                paymentMode = PaymentMode.UPI_FULL.name,
                upfrontPaidInr = 499.0,
                balanceDueOnDeliveryInr = 0.0,
                upiTransactionRef = "UTR-OTHER",
                screenshotBase64 = "",
                status = OrderStatus.PENDING_CONFIRMATION.name,
                adminNote = "Waiting",
                createdAt = 1000L,
                updatedAt = 1000L
            )
        )

        val sharedTransport = object : OrderSyncTransport {
            override val isConfigured: Boolean = true
            override suspend fun pushOrder(order: CustomerOrder, screenshotBase64: String): Result<Unit> {
                cloudSheet[order.orderNumber] = RemoteOrderRecord(
                    orderNumber = order.orderNumber,
                    customerName = order.customerName,
                    customerPhone = order.customerPhone,
                    deliveryAddress = order.deliveryAddress,
                    itemsJson = order.itemsJson,
                    totalAmountInr = order.totalAmountInr,
                    paymentMode = order.paymentMode,
                    upfrontPaidInr = order.upfrontPaidInr,
                    balanceDueOnDeliveryInr = order.balanceDueOnDeliveryInr,
                    upiTransactionRef = order.upiTransactionRef,
                    screenshotBase64 = screenshotBase64,
                    status = order.status,
                    adminNote = order.adminNote,
                    createdAt = order.createdAt,
                    updatedAt = order.updatedAt
                )
                return Result.success(Unit)
            }

            override suspend fun fetchOrders(): Result<List<RemoteOrderRecord>> {
                return Result.success(cloudSheet.values.toList())
            }

            override suspend fun decodeRemoteScreenshot(orderNumber: String, screenshotBase64: String): String? = null
        }

        val customerDb = Room.inMemoryDatabaseBuilder(context, MkBagsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val adminDb = Room.inMemoryDatabaseBuilder(context, MkBagsDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        try {
            val customerRepo = MkBagsRepository(customerDb, sharedTransport)
            val adminRepo = MkBagsRepository(adminDb, sharedTransport)

            // 1. Customer adds item and places order on customer phone
            val pid = customerRepo.saveProduct(
                Product(
                    name = "Olive Tote",
                    description = "Tote",
                    category = "Totes",
                    priceInr = 1000.0,
                    originalPriceInr = 1499.0,
                    stock = 5,
                    imageKey = "res:img_bag_tote"
                )
            )
            val prod = customerRepo.allProducts.first().first { it.id == pid }
            customerRepo.addToCart(prod, 1)
            val placed = customerRepo.placeOrder(
                customerName = "Ravi",
                customerPhone = "9876543210",
                deliveryAddress = "Hyderabad",
                cartItems = customerRepo.cartItemsWithProducts.first(),
                paymentMode = PaymentMode.COD_65_UPFRONT,
                codUpfrontPercent = 65,
                upiTransactionRef = "Screenshot Attached"
            ).getOrThrow()

            // Customer pushes order
            assertTrue(customerRepo.syncOrders(includeAllRemoteOrders = false).isSuccess)
            // Customer Orders tab must NOT include other customers' orders
            val customerVisible = customerRepo.customerOrders.first()
            assertEquals(1, customerVisible.size)
            assertEquals(placed.orderNumber, customerVisible[0].orderNumber)
            assertFalse(customerVisible[0].pendingSync)

            // 2. Business phone in Admin -> Orders pulls all orders from sheet
            assertTrue(adminRepo.syncOrders(includeAllRemoteOrders = true).isSuccess)
            val adminVisible = adminRepo.allOrders.first()
            assertEquals(2, adminVisible.size)
            val syncedForAdmin = adminVisible.first { it.orderNumber == placed.orderNumber }
            assertEquals("Ravi", syncedForAdmin.customerName)
            assertEquals(650.0, syncedForAdmin.upfrontPaidInr, 0.01)
            assertFalse(syncedForAdmin.isLocalCustomerOrder)

            // 3. Admin updates status & tracking note on business phone and syncs
            adminRepo.updateOrderStatus(syncedForAdmin.id, OrderStatus.SHIPPED, "Dispatched via BlueDart")
            assertTrue(adminRepo.syncOrders(includeAllRemoteOrders = true).isSuccess)

            // 4. Customer phone syncs and sees live SHIPPED update from admin
            assertTrue(customerRepo.syncOrders(includeAllRemoteOrders = false).isSuccess)
            val updatedCustomerOrder = customerRepo.customerOrders.first().first { it.orderNumber == placed.orderNumber }
            assertEquals(OrderStatus.SHIPPED, updatedCustomerOrder.orderStatus)
            assertEquals("Dispatched via BlueDart", updatedCustomerOrder.adminNote)
        } finally {
            customerDb.close()
            adminDb.close()
        }
    }
}
