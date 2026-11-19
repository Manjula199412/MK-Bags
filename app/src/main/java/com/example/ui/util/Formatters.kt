package com.example.ui.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

fun formatInr(amount: Double): String {
    val rounded = amount.roundToInt()
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
    return "₹${formatter.format(rounded)}"
}

fun formatTimestamp(millis: Long): String {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}

val PRESET_IMAGE_OPTIONS = listOf(
    "res:img_bag_tote" to "Tan Leather Tote",
    "res:img_bag_combo" to "3-Piece Combo Set",
    "res:img_bag_backpack" to "Commuter Backpack Duo",
    "res:img_hero_promo" to "Boutique Flatlay"
)
