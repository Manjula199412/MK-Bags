package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val category: String,
    val priceInr: Double,
    val originalPriceInr: Double,
    val stock: Int,
    val imageKey: String,
    val isCombo: Boolean = false,
    val isBudgetDeal: Boolean = false,
    val isLimitedOffer: Boolean = false,
    val isActive: Boolean = true,
    val badgeText: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val discountPercent: Int
        get() = if (originalPriceInr > priceInr && originalPriceInr > 0) {
            (((originalPriceInr - priceInr) / originalPriceInr) * 100).toInt()
        } else 0
}
