package com.example.data.model

enum class ThemePreset(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val previewPrimaryHex: Long,
    val previewSecondaryHex: Long,
    val previewBackgroundHex: Long
) {
    ESPRESSO_LUXE("ESPRESSO_LUXE", "Espresso Luxe", "Warm saddle leather & cream", 0xFF4A2C20, 0xFFC58940, 0xFFFCF8F5),
    TERRACOTTA_WARM("TERRACOTTA_WARM", "Terracotta Clay", "Sunbaked clay & sand", 0xFFB84A27, 0xFF7B5E3B, 0xFFFFF8F5),
    MIDNIGHT_INDIGO("MIDNIGHT_INDIGO", "Midnight Indigo", "Editorial cobalt & slate", 0xFF283593, 0xFF00897B, 0xFFF6F8FC),
    MATCHA_MINIMAL("MATCHA_MINIMAL", "Matcha Minimal", "Botanical olive & ivory", 0xFF3E5C3A, 0xFFA47148, 0xFFF7FAF5),
    ROSE_VELVET("ROSE_VELVET", "Rose Velvet", "Berry plum & champagne", 0xFF8E244D, 0xFFD49B54, 0xFFFFF6F9);

    companion object {
        fun fromId(id: String): ThemePreset = entries.firstOrNull { it.id == id } ?: ESPRESSO_LUXE
    }
}

enum class FontStylePreset(
    val id: String,
    val displayName: String,
    val description: String
) {
    EDITORIAL("EDITORIAL", "Editorial Serif", "Playfair Display headings + clean sans"),
    MODERN_CLEAN("MODERN_CLEAN", "Modern Clean", "Plus Jakarta Sans all around"),
    BOLD_STREET("BOLD_STREET", "Bold Street", "Space Grotesk geometric trend"),
    SOFT_ROUNDED("SOFT_ROUNDED", "Soft Boutique", "Outfit smooth contemporary");

    companion object {
        fun fromId(id: String): FontStylePreset = entries.firstOrNull { it.id == id } ?: EDITORIAL
    }
}

enum class ColorModeOption(val id: String, val label: String) {
    SYSTEM("SYSTEM", "System"),
    LIGHT("LIGHT", "Light"),
    DARK("DARK", "Dark");

    companion object {
        fun fromId(id: String): ColorModeOption = entries.firstOrNull { it.id == id } ?: LIGHT
    }
}

enum class CornerStyleOption(val id: String, val label: String, val cardRadiusDp: Int) {
    ROUNDED("ROUNDED", "Soft Rounded", 18),
    SHARP("SHARP", "Crisp Editorial", 6),
    PILL("PILL", "Plush Curve", 26);

    companion object {
        fun fromId(id: String): CornerStyleOption = entries.firstOrNull { it.id == id } ?: ROUNDED
    }
}

data class StoreConfig(
    // Branding & Storefront Copy
    val storeName: String = "MK Bags",
    val storeTagline: String = "Curated Everyday Bags, Combos & Budget Picks",
    val heroBannerBadge: String = "LIMITED-TIME BUNDLE",
    val heroBannerTitle: String = "Weekend Combo Drop",
    val heroBannerSubtitle: String = "Save up to 45% on 3-piece bag combos & daily totes",
    val heroBannerImageKey: String = "res:img_hero_promo",
    val heroCountdownHours: Int = 18,
    val showPromoSection: Boolean = true,
    val promoSectionTitle: String = "Bundle Deals & Combos",
    val budgetSectionTitle: String = "Budget Picks",
    val allBagsSectionTitle: String = "All Bags",
    val checkoutNotice: String = "COD orders require 65% upfront via UPI before dispatch.",

    // Theme & Typography
    val themePresetId: String = ThemePreset.ESPRESSO_LUXE.id,
    val fontStyleId: String = FontStylePreset.EDITORIAL.id,
    val colorModeId: String = ColorModeOption.LIGHT.id,
    val cornerStyleId: String = CornerStyleOption.ROUNDED.id,

    // Search & Filters Customization
    val customCategoriesCsv: String = "Totes,Combos,Backpacks,Slings,Travel,Budget Picks",
    val enableCategoryFilter: Boolean = true,
    val enablePriceFilter: Boolean = true,
    val enableComboFilter: Boolean = true,
    val enableInStockFilter: Boolean = true,
    val enableSortFilter: Boolean = true,
    val budgetPriceThresholdInr: Int = 599,
    val maxPriceFilterCapInr: Int = 2999,

    // Payments & Support
    val merchantPhone: String = "7799248919",
    val upiId: String = "7799248919-m0b1@ybl",
    val upiPayeeName: String = "MK Bags",
    val codUpfrontPercent: Int = 65,
    val enableCodOption: Boolean = true,
    val enableFullUpiOption: Boolean = true,

    // Admin Password
    val adminPassword: String = "7799",

    // Customer Profile & Preferences
    val customerName: String = "",
    val customerPhone: String = "",
    val customerAddress: String = "",
    val customerDealAlertsEnabled: Boolean = true
) {
    val categoriesList: List<String>
        get() = customCategoriesCsv
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

    val themePreset: ThemePreset get() = ThemePreset.fromId(themePresetId)
    val fontStyle: FontStylePreset get() = FontStylePreset.fromId(fontStyleId)
    val colorMode: ColorModeOption get() = ColorModeOption.fromId(colorModeId)
    val cornerStyle: CornerStyleOption get() = CornerStyleOption.fromId(cornerStyleId)
}
