package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

enum class OrderStatus(
    val label: String,
    val description: String,
    val stepIndex: Int
) {
    PENDING_CONFIRMATION("Pending Confirmation", "Waiting for MK Bags to verify payment & confirm", 0),
    CONFIRMED("Confirmed", "Order verified and accepted by MK Bags", 1),
    PACKING("Packing", "Your bags are being quality-checked and packed", 2),
    SHIPPED("Shipped", "Dispatched from MK Bags studio", 3),
    OUT_FOR_DELIVERY("Out for Delivery", "Courier is on the way to your address", 4),
    DELIVERED("Delivered", "Order delivered safely", 5),
    CANCELLED("Cancelled", "Order was cancelled", -1);

    companion object {
        fun fromRaw(raw: String): OrderStatus =
            entries.firstOrNull { it.name == raw } ?: PENDING_CONFIRMATION

        val activeTimelineSteps: List<OrderStatus> = listOf(
            PENDING_CONFIRMATION,
            CONFIRMED,
            PACKING,
            SHIPPED,
            OUT_FOR_DELIVERY,
            DELIVERED
        )
    }
}

enum class PaymentMode(val label: String) {
    UPI_FULL("Full UPI Payment (100%)"),
    COD_65_UPFRONT("Cash on Delivery (65% Upfront + 35% COD)");

    companion object {
        fun fromRaw(raw: String): PaymentMode =
            entries.firstOrNull { it.name == raw } ?: UPI_FULL
    }
}

data class OrderLineItem(
    val productId: Long,
    val name: String,
    val quantity: Int,
    val unitPriceInr: Double
) {
    val lineTotalInr: Double get() = unitPriceInr * quantity
}

@Entity(tableName = "orders")
data class CustomerOrder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderNumber: String,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val itemsJson: String,
    val totalAmountInr: Double,
    val paymentMode: String,
    val upfrontPaidInr: Double,
    val balanceDueOnDeliveryInr: Double,
    val upiTransactionRef: String,
    @ColumnInfo(defaultValue = "")
    val paymentScreenshotKey: String = "",
    val status: String = OrderStatus.PENDING_CONFIRMATION.name,
    val adminNote: String = "",
    @ColumnInfo(defaultValue = "1")
    val isLocalCustomerOrder: Boolean = true,
    @ColumnInfo(defaultValue = "1")
    val pendingSync: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val orderStatus: OrderStatus get() = OrderStatus.fromRaw(status)
    val paymentModeEnum: PaymentMode get() = PaymentMode.fromRaw(paymentMode)

    fun parseLineItems(): List<OrderLineItem> {
        return try {
            val array = JSONArray(itemsJson)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    add(
                        OrderLineItem(
                            productId = obj.optLong("productId", 0L),
                            name = obj.optString("name", "Bag"),
                            quantity = obj.optInt("quantity", 1),
                            unitPriceInr = obj.optDouble("unitPriceInr", 0.0)
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    companion object {
        fun encodeLineItems(items: List<OrderLineItem>): String {
            val array = JSONArray()
            items.forEach { item ->
                array.put(
                    JSONObject().apply {
                        put("productId", item.productId)
                        put("name", item.name)
                        put("quantity", item.quantity)
                        put("unitPriceInr", item.unitPriceInr)
                    }
                )
            }
            return array.toString()
        }
    }
}
