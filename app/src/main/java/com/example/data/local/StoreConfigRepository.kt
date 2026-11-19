package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.StoreConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.storeConfigDataStore: DataStore<Preferences> by preferencesDataStore(name = "mk_bags_store_config")

class StoreConfigRepository(private val context: Context) {

    private object Keys {
        val STORE_NAME = stringPreferencesKey("store_name")
        val STORE_TAGLINE = stringPreferencesKey("store_tagline")
        val HERO_BADGE = stringPreferencesKey("hero_badge")
        val HERO_TITLE = stringPreferencesKey("hero_title")
        val HERO_SUBTITLE = stringPreferencesKey("hero_subtitle")
        val HERO_IMAGE_KEY = stringPreferencesKey("hero_image_key")
        val HERO_COUNTDOWN_HOURS = intPreferencesKey("hero_countdown_hours")
        val SHOW_PROMO_SECTION = booleanPreferencesKey("show_promo_section")
        val PROMO_SECTION_TITLE = stringPreferencesKey("promo_section_title")
        val BUDGET_SECTION_TITLE = stringPreferencesKey("budget_section_title")
        val ALL_BAGS_SECTION_TITLE = stringPreferencesKey("all_bags_section_title")
        val CHECKOUT_NOTICE = stringPreferencesKey("checkout_notice")

        val THEME_PRESET_ID = stringPreferencesKey("theme_preset_id")
        val FONT_STYLE_ID = stringPreferencesKey("font_style_id")
        val COLOR_MODE_ID = stringPreferencesKey("color_mode_id")
        val CORNER_STYLE_ID = stringPreferencesKey("corner_style_id")

        val CUSTOM_CATEGORIES_CSV = stringPreferencesKey("custom_categories_csv")
        val ENABLE_CATEGORY_FILTER = booleanPreferencesKey("enable_category_filter")
        val ENABLE_PRICE_FILTER = booleanPreferencesKey("enable_price_filter")
        val ENABLE_COMBO_FILTER = booleanPreferencesKey("enable_combo_filter")
        val ENABLE_IN_STOCK_FILTER = booleanPreferencesKey("enable_in_stock_filter")
        val ENABLE_SORT_FILTER = booleanPreferencesKey("enable_sort_filter")
        val BUDGET_PRICE_THRESHOLD = intPreferencesKey("budget_price_threshold")
        val MAX_PRICE_FILTER_CAP = intPreferencesKey("max_price_filter_cap")

        val MERCHANT_PHONE = stringPreferencesKey("merchant_phone")
        val UPI_ID = stringPreferencesKey("upi_id")
        val UPI_PAYEE_NAME = stringPreferencesKey("upi_payee_name")
        val COD_UPFRONT_PERCENT = intPreferencesKey("cod_upfront_percent")
        val ENABLE_COD_OPTION = booleanPreferencesKey("enable_cod_option")
        val ENABLE_FULL_UPI_OPTION = booleanPreferencesKey("enable_full_upi_option")

        val ADMIN_PASSWORD = stringPreferencesKey("admin_password")

        val CUSTOMER_NAME = stringPreferencesKey("customer_name")
        val CUSTOMER_PHONE = stringPreferencesKey("customer_phone")
        val CUSTOMER_ADDRESS = stringPreferencesKey("customer_address")
        val CUSTOMER_DEAL_ALERTS_ENABLED = booleanPreferencesKey("customer_deal_alerts_enabled")
    }

    val storeConfig: Flow<StoreConfig> = context.storeConfigDataStore.data.map { prefs ->
        val defaults = StoreConfig()
        StoreConfig(
            storeName = prefs[Keys.STORE_NAME] ?: defaults.storeName,
            storeTagline = prefs[Keys.STORE_TAGLINE] ?: defaults.storeTagline,
            heroBannerBadge = prefs[Keys.HERO_BADGE] ?: defaults.heroBannerBadge,
            heroBannerTitle = prefs[Keys.HERO_TITLE] ?: defaults.heroBannerTitle,
            heroBannerSubtitle = prefs[Keys.HERO_SUBTITLE] ?: defaults.heroBannerSubtitle,
            heroBannerImageKey = prefs[Keys.HERO_IMAGE_KEY] ?: defaults.heroBannerImageKey,
            heroCountdownHours = prefs[Keys.HERO_COUNTDOWN_HOURS] ?: defaults.heroCountdownHours,
            showPromoSection = prefs[Keys.SHOW_PROMO_SECTION] ?: defaults.showPromoSection,
            promoSectionTitle = prefs[Keys.PROMO_SECTION_TITLE] ?: defaults.promoSectionTitle,
            budgetSectionTitle = prefs[Keys.BUDGET_SECTION_TITLE] ?: defaults.budgetSectionTitle,
            allBagsSectionTitle = prefs[Keys.ALL_BAGS_SECTION_TITLE] ?: defaults.allBagsSectionTitle,
            checkoutNotice = prefs[Keys.CHECKOUT_NOTICE] ?: defaults.checkoutNotice,
            themePresetId = prefs[Keys.THEME_PRESET_ID] ?: defaults.themePresetId,
            fontStyleId = prefs[Keys.FONT_STYLE_ID] ?: defaults.fontStyleId,
            colorModeId = prefs[Keys.COLOR_MODE_ID] ?: defaults.colorModeId,
            cornerStyleId = prefs[Keys.CORNER_STYLE_ID] ?: defaults.cornerStyleId,
            customCategoriesCsv = prefs[Keys.CUSTOM_CATEGORIES_CSV] ?: defaults.customCategoriesCsv,
            enableCategoryFilter = prefs[Keys.ENABLE_CATEGORY_FILTER] ?: defaults.enableCategoryFilter,
            enablePriceFilter = prefs[Keys.ENABLE_PRICE_FILTER] ?: defaults.enablePriceFilter,
            enableComboFilter = prefs[Keys.ENABLE_COMBO_FILTER] ?: defaults.enableComboFilter,
            enableInStockFilter = prefs[Keys.ENABLE_IN_STOCK_FILTER] ?: defaults.enableInStockFilter,
            enableSortFilter = prefs[Keys.ENABLE_SORT_FILTER] ?: defaults.enableSortFilter,
            budgetPriceThresholdInr = prefs[Keys.BUDGET_PRICE_THRESHOLD] ?: defaults.budgetPriceThresholdInr,
            maxPriceFilterCapInr = prefs[Keys.MAX_PRICE_FILTER_CAP] ?: defaults.maxPriceFilterCapInr,
            merchantPhone = prefs[Keys.MERCHANT_PHONE] ?: defaults.merchantPhone,
            upiId = prefs[Keys.UPI_ID] ?: defaults.upiId,
            upiPayeeName = prefs[Keys.UPI_PAYEE_NAME] ?: defaults.upiPayeeName,
            codUpfrontPercent = prefs[Keys.COD_UPFRONT_PERCENT] ?: defaults.codUpfrontPercent,
            enableCodOption = prefs[Keys.ENABLE_COD_OPTION] ?: defaults.enableCodOption,
            enableFullUpiOption = prefs[Keys.ENABLE_FULL_UPI_OPTION] ?: defaults.enableFullUpiOption,
            adminPassword = prefs[Keys.ADMIN_PASSWORD] ?: defaults.adminPassword,
            customerName = prefs[Keys.CUSTOMER_NAME] ?: defaults.customerName,
            customerPhone = prefs[Keys.CUSTOMER_PHONE] ?: defaults.customerPhone,
            customerAddress = prefs[Keys.CUSTOMER_ADDRESS] ?: defaults.customerAddress,
            customerDealAlertsEnabled = prefs[Keys.CUSTOMER_DEAL_ALERTS_ENABLED] ?: defaults.customerDealAlertsEnabled
        )
    }

    suspend fun updateStoreConfig(transform: (StoreConfig) -> StoreConfig) {
        context.storeConfigDataStore.edit { prefs ->
            val current = StoreConfig(
                storeName = prefs[Keys.STORE_NAME] ?: StoreConfig().storeName,
                storeTagline = prefs[Keys.STORE_TAGLINE] ?: StoreConfig().storeTagline,
                heroBannerBadge = prefs[Keys.HERO_BADGE] ?: StoreConfig().heroBannerBadge,
                heroBannerTitle = prefs[Keys.HERO_TITLE] ?: StoreConfig().heroBannerTitle,
                heroBannerSubtitle = prefs[Keys.HERO_SUBTITLE] ?: StoreConfig().heroBannerSubtitle,
                heroBannerImageKey = prefs[Keys.HERO_IMAGE_KEY] ?: StoreConfig().heroBannerImageKey,
                heroCountdownHours = prefs[Keys.HERO_COUNTDOWN_HOURS] ?: StoreConfig().heroCountdownHours,
                showPromoSection = prefs[Keys.SHOW_PROMO_SECTION] ?: StoreConfig().showPromoSection,
                promoSectionTitle = prefs[Keys.PROMO_SECTION_TITLE] ?: StoreConfig().promoSectionTitle,
                budgetSectionTitle = prefs[Keys.BUDGET_SECTION_TITLE] ?: StoreConfig().budgetSectionTitle,
                allBagsSectionTitle = prefs[Keys.ALL_BAGS_SECTION_TITLE] ?: StoreConfig().allBagsSectionTitle,
                checkoutNotice = prefs[Keys.CHECKOUT_NOTICE] ?: StoreConfig().checkoutNotice,
                themePresetId = prefs[Keys.THEME_PRESET_ID] ?: StoreConfig().themePresetId,
                fontStyleId = prefs[Keys.FONT_STYLE_ID] ?: StoreConfig().fontStyleId,
                colorModeId = prefs[Keys.COLOR_MODE_ID] ?: StoreConfig().colorModeId,
                cornerStyleId = prefs[Keys.CORNER_STYLE_ID] ?: StoreConfig().cornerStyleId,
                customCategoriesCsv = prefs[Keys.CUSTOM_CATEGORIES_CSV] ?: StoreConfig().customCategoriesCsv,
                enableCategoryFilter = prefs[Keys.ENABLE_CATEGORY_FILTER] ?: StoreConfig().enableCategoryFilter,
                enablePriceFilter = prefs[Keys.ENABLE_PRICE_FILTER] ?: StoreConfig().enablePriceFilter,
                enableComboFilter = prefs[Keys.ENABLE_COMBO_FILTER] ?: StoreConfig().enableComboFilter,
                enableInStockFilter = prefs[Keys.ENABLE_IN_STOCK_FILTER] ?: StoreConfig().enableInStockFilter,
                enableSortFilter = prefs[Keys.ENABLE_SORT_FILTER] ?: StoreConfig().enableSortFilter,
                budgetPriceThresholdInr = prefs[Keys.BUDGET_PRICE_THRESHOLD] ?: StoreConfig().budgetPriceThresholdInr,
                maxPriceFilterCapInr = prefs[Keys.MAX_PRICE_FILTER_CAP] ?: StoreConfig().maxPriceFilterCapInr,
                merchantPhone = prefs[Keys.MERCHANT_PHONE] ?: StoreConfig().merchantPhone,
                upiId = prefs[Keys.UPI_ID] ?: StoreConfig().upiId,
                upiPayeeName = prefs[Keys.UPI_PAYEE_NAME] ?: StoreConfig().upiPayeeName,
                codUpfrontPercent = prefs[Keys.COD_UPFRONT_PERCENT] ?: StoreConfig().codUpfrontPercent,
                enableCodOption = prefs[Keys.ENABLE_COD_OPTION] ?: StoreConfig().enableCodOption,
                enableFullUpiOption = prefs[Keys.ENABLE_FULL_UPI_OPTION] ?: StoreConfig().enableFullUpiOption,
                adminPassword = prefs[Keys.ADMIN_PASSWORD] ?: StoreConfig().adminPassword,
                customerName = prefs[Keys.CUSTOMER_NAME] ?: StoreConfig().customerName,
                customerPhone = prefs[Keys.CUSTOMER_PHONE] ?: StoreConfig().customerPhone,
                customerAddress = prefs[Keys.CUSTOMER_ADDRESS] ?: StoreConfig().customerAddress,
                customerDealAlertsEnabled = prefs[Keys.CUSTOMER_DEAL_ALERTS_ENABLED] ?: StoreConfig().customerDealAlertsEnabled
            )
            val updated = transform(current)
            prefs[Keys.STORE_NAME] = updated.storeName
            prefs[Keys.STORE_TAGLINE] = updated.storeTagline
            prefs[Keys.HERO_BADGE] = updated.heroBannerBadge
            prefs[Keys.HERO_TITLE] = updated.heroBannerTitle
            prefs[Keys.HERO_SUBTITLE] = updated.heroBannerSubtitle
            prefs[Keys.HERO_IMAGE_KEY] = updated.heroBannerImageKey
            prefs[Keys.HERO_COUNTDOWN_HOURS] = updated.heroCountdownHours
            prefs[Keys.SHOW_PROMO_SECTION] = updated.showPromoSection
            prefs[Keys.PROMO_SECTION_TITLE] = updated.promoSectionTitle
            prefs[Keys.BUDGET_SECTION_TITLE] = updated.budgetSectionTitle
            prefs[Keys.ALL_BAGS_SECTION_TITLE] = updated.allBagsSectionTitle
            prefs[Keys.CHECKOUT_NOTICE] = updated.checkoutNotice
            prefs[Keys.THEME_PRESET_ID] = updated.themePresetId
            prefs[Keys.FONT_STYLE_ID] = updated.fontStyleId
            prefs[Keys.COLOR_MODE_ID] = updated.colorModeId
            prefs[Keys.CORNER_STYLE_ID] = updated.cornerStyleId
            prefs[Keys.CUSTOM_CATEGORIES_CSV] = updated.customCategoriesCsv
            prefs[Keys.ENABLE_CATEGORY_FILTER] = updated.enableCategoryFilter
            prefs[Keys.ENABLE_PRICE_FILTER] = updated.enablePriceFilter
            prefs[Keys.ENABLE_COMBO_FILTER] = updated.enableComboFilter
            prefs[Keys.ENABLE_IN_STOCK_FILTER] = updated.enableInStockFilter
            prefs[Keys.ENABLE_SORT_FILTER] = updated.enableSortFilter
            prefs[Keys.BUDGET_PRICE_THRESHOLD] = updated.budgetPriceThresholdInr
            prefs[Keys.MAX_PRICE_FILTER_CAP] = updated.maxPriceFilterCapInr
            prefs[Keys.MERCHANT_PHONE] = updated.merchantPhone
            prefs[Keys.UPI_ID] = updated.upiId
            prefs[Keys.UPI_PAYEE_NAME] = updated.upiPayeeName
            prefs[Keys.COD_UPFRONT_PERCENT] = updated.codUpfrontPercent
            prefs[Keys.ENABLE_COD_OPTION] = updated.enableCodOption
            prefs[Keys.ENABLE_FULL_UPI_OPTION] = updated.enableFullUpiOption
            prefs[Keys.ADMIN_PASSWORD] = updated.adminPassword
            prefs[Keys.CUSTOMER_NAME] = updated.customerName
            prefs[Keys.CUSTOMER_PHONE] = updated.customerPhone
            prefs[Keys.CUSTOMER_ADDRESS] = updated.customerAddress
            prefs[Keys.CUSTOMER_DEAL_ALERTS_ENABLED] = updated.customerDealAlertsEnabled
        }
    }
}
