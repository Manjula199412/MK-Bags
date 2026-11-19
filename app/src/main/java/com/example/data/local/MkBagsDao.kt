package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CartItem
import com.example.data.model.CustomerOrder
import com.example.data.model.DealAlert
import com.example.data.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY isLimitedOffer DESC, isCombo DESC, createdAt DESC")
    fun observeAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY isLimitedOffer DESC, isCombo DESC, createdAt DESC")
    fun observeActiveProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Query("UPDATE products SET stock = MAX(0, stock + :delta) WHERE id = :productId")
    suspend fun adjustStock(productId: Long, delta: Int)

    @Query("UPDATE products SET stock = MAX(0, stock - :qty) WHERE id = :productId")
    suspend fun decrementStock(productId: Long, qty: Int)

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProductById(productId: Long)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun countProducts(): Int
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items ORDER BY addedAt ASC")
    fun observeCartItems(): Flow<List<CartItem>>

    @Query("SELECT * FROM cart_items WHERE productId = :productId LIMIT 1")
    suspend fun getCartItem(productId: Long): CartItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCartItem(item: CartItem)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun removeCartItem(productId: Long)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun observeAllOrders(): Flow<List<CustomerOrder>>

    @Query("SELECT * FROM orders WHERE isLocalCustomerOrder = 1 ORDER BY createdAt DESC")
    fun observeCustomerOrders(): Flow<List<CustomerOrder>>

    @Query("SELECT * FROM orders ORDER BY createdAt DESC LIMIT 1")
    suspend fun getMostRecentOrder(): CustomerOrder?

    @Query("SELECT * FROM orders WHERE isLocalCustomerOrder = 1 ORDER BY createdAt DESC LIMIT 1")
    suspend fun getMostRecentCustomerOrder(): CustomerOrder?

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: Long): CustomerOrder?

    @Query("SELECT * FROM orders WHERE orderNumber = :orderNumber LIMIT 1")
    suspend fun getOrderByNumber(orderNumber: String): CustomerOrder?

    @Query("SELECT * FROM orders WHERE pendingSync = 1 ORDER BY updatedAt ASC")
    suspend fun getPendingSyncOrders(): List<CustomerOrder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: CustomerOrder): Long

    @Update
    suspend fun updateOrder(order: CustomerOrder)

    @Query("UPDATE orders SET status = :status, adminNote = :adminNote, pendingSync = 1, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: String, adminNote: String, updatedAt: Long)

    @Query("UPDATE orders SET paymentScreenshotKey = :screenshotKey, pendingSync = 1, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateOrderPaymentScreenshot(orderId: Long, screenshotKey: String, updatedAt: Long)

    @Query("UPDATE orders SET pendingSync = 0 WHERE id = :orderId")
    suspend fun markOrderSynced(orderId: Long)

    @Query("SELECT COUNT(*) FROM orders")
    suspend fun countOrders(): Int
}

@Dao
interface DealAlertDao {
    @Query("SELECT * FROM deal_alerts ORDER BY createdAt DESC")
    fun observeDealAlerts(): Flow<List<DealAlert>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: DealAlert): Long

    @Delete
    suspend fun deleteAlert(alert: DealAlert)

    @Query("SELECT COUNT(*) FROM deal_alerts")
    suspend fun countAlerts(): Int
}
