package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deal_alerts")
data class DealAlert(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val promoCode: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
