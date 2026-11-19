package com.example.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.ColorModeOption
import com.example.data.model.CornerStyleOption
import com.example.data.model.FontStylePreset
import com.example.data.model.StoreConfig
import com.example.data.model.ThemePreset
import com.example.ui.components.BagImage
import com.example.ui.util.PRESET_IMAGE_OPTIONS

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AdminCustomizeTab(
    storeConfig: StoreConfig,
    onUpdateStoreConfig: (message: String?, (StoreConfig) -> StoreConfig) -> Unit,
    onImportPickedPhoto: (Uri, String, (String) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableIntStateOf(0) }
    val sections = listOf("Theme", "Storefront", "Filters", "Pay & PIN")

    Column(modifier = modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = selectedSection) {
            sections.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSection == index,
                    onClick = { selectedSection = index },
                    text = { Text(title, maxLines = 1) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedSection) {
                0 -> {
                    // Theme & Typography Customization
                    item {
                        SectionCard(title = "Trend Color Palette", icon = Icons.Filled.Palette) {
                            ThemePreset.entries.forEach { preset ->
                                val selected = storeConfig.themePresetId == preset.id
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onUpdateStoreConfig("Theme set to ${preset.displayName}") {
                                                it.copy(themePresetId = preset.id)
                                            }
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                    ),
                                    border = BorderStroke(
                                        if (selected) 2.dp else 1.dp,
                                        if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Box(
                                                    Modifier
                                                        .size(22.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(preset.previewPrimaryHex))
                                                )
                                                Box(
                                                    Modifier
                                                        .size(22.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(preset.previewSecondaryHex))
                                                )
                                                Box(
                                                    Modifier
                                                        .size(22.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(preset.previewBackgroundHex))
                                                        .border(1.dp, Color.Gray.copy(alpha = 0.4f), CircleShape)
                                                )
                                            }
                                            Column {
                                                Text(preset.displayName, style = MaterialTheme.typography.titleSmall)
                                                Text(
                                                    preset.subtitle,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        if (selected) {
                                            Icon(Icons.Filled.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        SectionCard(title = "Typography & Text Trend", icon = Icons.Filled.Palette) {
                            FontStylePreset.entries.forEach { font ->
                                val selected = storeConfig.fontStyleId == font.id
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        onUpdateStoreConfig("Font switched to ${font.displayName}") {
                                            it.copy(fontStyleId = font.id)
                                        }
                                    },
                                    label = { Text("${font.displayName} — ${font.description}") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(Modifier.height(6.dp))
                            Text("Brightness Mode", style = MaterialTheme.typography.labelMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ColorModeOption.entries.forEach { mode ->
                                    FilterChip(
                                        selected = storeConfig.colorModeId == mode.id,
                                        onClick = {
                                            onUpdateStoreConfig("Mode set to ${mode.label}") {
                                                it.copy(colorModeId = mode.id)
                                            }
                                        },
                                        label = { Text(mode.label) }
                                    )
                                }
                            }

                            Text("Card & Button Corner Style", style = MaterialTheme.typography.labelMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CornerStyleOption.entries.forEach { corner ->
                                    FilterChip(
                                        selected = storeConfig.cornerStyleId == corner.id,
                                        onClick = {
                                            onUpdateStoreConfig("Corners set to ${corner.label}") {
                                                it.copy(cornerStyleId = corner.id)
                                            }
                                        },
                                        label = { Text(corner.label) }
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Storefront Copy & Editable Hero Banner Photo
                    item {
                        StorefrontCustomizationSection(
                            storeConfig = storeConfig,
                            onSave = { updated ->
                                onUpdateStoreConfig("Storefront updated!") { updated }
                            },
                            onImportPickedPhoto = onImportPickedPhoto
                        )
                    }
                }

                2 -> {
                    // Search & Filters Customization
                    item {
                        FiltersCustomizationSection(
                            storeConfig = storeConfig,
                            onSave = { updated ->
                                onUpdateStoreConfig("Search & filter settings updated!") { updated }
                            }
                        )
                    }
                }

                3 -> {
                    // Payments & Admin Password
                    item {
                        PaymentsAndSecuritySection(
                            storeConfig = storeConfig,
                            onSave = { msg, updated ->
                                onUpdateStoreConfig(msg) { updated }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StorefrontCustomizationSection(
    storeConfig: StoreConfig,
    onSave: (StoreConfig) -> Unit,
    onImportPickedPhoto: (Uri, String, (String) -> Unit) -> Unit
) {
    var storeName by remember(storeConfig.storeName) { mutableStateOf(storeConfig.storeName) }
    var tagline by remember(storeConfig.storeTagline) { mutableStateOf(storeConfig.storeTagline) }
    var showPromo by remember(storeConfig.showPromoSection) { mutableStateOf(storeConfig.showPromoSection) }
    var heroBadge by remember(storeConfig.heroBannerBadge) { mutableStateOf(storeConfig.heroBannerBadge) }
    var heroTitle by remember(storeConfig.heroBannerTitle) { mutableStateOf(storeConfig.heroBannerTitle) }
    var heroSubtitle by remember(storeConfig.heroBannerSubtitle) { mutableStateOf(storeConfig.heroBannerSubtitle) }
    var heroImageKey by remember(storeConfig.heroBannerImageKey) { mutableStateOf(storeConfig.heroBannerImageKey) }
    var countdownHours by remember(storeConfig.heroCountdownHours) { mutableStateOf(storeConfig.heroCountdownHours.toString()) }
    var promoSectionTitle by remember(storeConfig.promoSectionTitle) { mutableStateOf(storeConfig.promoSectionTitle) }
    var budgetSectionTitle by remember(storeConfig.budgetSectionTitle) { mutableStateOf(storeConfig.budgetSectionTitle) }
    var allBagsTitle by remember(storeConfig.allBagsSectionTitle) { mutableStateOf(storeConfig.allBagsSectionTitle) }

    val bannerPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onImportPickedPhoto(uri, "banner") { savedKey ->
                heroImageKey = savedKey
            }
        }
    }

    SectionCard(title = "Storefront & Promotional Banner", icon = Icons.Filled.Storefront) {
        OutlinedTextField(
            value = storeName,
            onValueChange = { storeName = it },
            label = { Text("Store Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = tagline,
            onValueChange = { tagline = it },
            label = { Text("Storefront Tagline") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Show Limited-Time Promo Banner", style = MaterialTheme.typography.bodyMedium)
            Switch(checked = showPromo, onCheckedChange = { showPromo = it })
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BagImage(
                imageKey = heroImageKey,
                contentDescription = "Hero banner preview",
                modifier = Modifier
                    .size(width = 110.dp, height = 66.dp)
                    .clip(MaterialTheme.shapes.small)
            )
            OutlinedButton(
                onClick = {
                    bannerPhotoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            ) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(6.dp))
                Text("Edit Banner Photo")
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(PRESET_IMAGE_OPTIONS) { (key, label) ->
                FilterChip(
                    selected = heroImageKey == key,
                    onClick = { heroImageKey = key },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        OutlinedTextField(
            value = heroBadge,
            onValueChange = { heroBadge = it },
            label = { Text("Promo Badge Text") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = heroTitle,
            onValueChange = { heroTitle = it },
            label = { Text("Promo Banner Headline") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = heroSubtitle,
            onValueChange = { heroSubtitle = it },
            label = { Text("Promo Banner Subheadline") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = countdownHours,
            onValueChange = { countdownHours = it.filter { c -> c.isDigit() } },
            label = { Text("Offer Countdown Hours") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = promoSectionTitle,
            onValueChange = { promoSectionTitle = it },
            label = { Text("Combo Section Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = budgetSectionTitle,
            onValueChange = { budgetSectionTitle = it },
            label = { Text("Budget Section Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = allBagsTitle,
            onValueChange = { allBagsTitle = it },
            label = { Text("All Bags Section Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                onSave(
                    storeConfig.copy(
                        storeName = storeName.trim().ifBlank { "MK Bags" },
                        storeTagline = tagline.trim(),
                        showPromoSection = showPromo,
                        heroBannerBadge = heroBadge.trim(),
                        heroBannerTitle = heroTitle.trim(),
                        heroBannerSubtitle = heroSubtitle.trim(),
                        heroBannerImageKey = heroImageKey,
                        heroCountdownHours = countdownHours.toIntOrNull()?.coerceIn(1, 240) ?: 18,
                        promoSectionTitle = promoSectionTitle.trim().ifBlank { "Bundle Deals & Combos" },
                        budgetSectionTitle = budgetSectionTitle.trim().ifBlank { "Budget Picks" },
                        allBagsSectionTitle = allBagsTitle.trim().ifBlank { "All Bags" }
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("save_storefront_customization_button")
        ) {
            Text("Apply Storefront Changes")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FiltersCustomizationSection(
    storeConfig: StoreConfig,
    onSave: (StoreConfig) -> Unit
) {
    var categories by remember(storeConfig.customCategoriesCsv) { mutableStateOf(storeConfig.categoriesList) }
    var newCategory by remember { mutableStateOf("") }
    var enableCat by remember(storeConfig.enableCategoryFilter) { mutableStateOf(storeConfig.enableCategoryFilter) }
    var enablePrice by remember(storeConfig.enablePriceFilter) { mutableStateOf(storeConfig.enablePriceFilter) }
    var enableCombo by remember(storeConfig.enableComboFilter) { mutableStateOf(storeConfig.enableComboFilter) }
    var enableInStock by remember(storeConfig.enableInStockFilter) { mutableStateOf(storeConfig.enableInStockFilter) }
    var enableSort by remember(storeConfig.enableSortFilter) { mutableStateOf(storeConfig.enableSortFilter) }
    var budgetCap by remember(storeConfig.budgetPriceThresholdInr) { mutableStateOf(storeConfig.budgetPriceThresholdInr.toString()) }
    var maxCap by remember(storeConfig.maxPriceFilterCapInr) { mutableStateOf(storeConfig.maxPriceFilterCapInr.toString()) }

    SectionCard(title = "Customize Search & Filters", icon = Icons.Filled.Tune) {
        Text("Category Filter Tags", style = MaterialTheme.typography.labelMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { cat ->
                InputChip(
                    selected = false,
                    onClick = {},
                    label = { Text(cat) },
                    trailingIcon = {
                        IconButton(
                            onClick = { categories = categories.filterNot { it == cat } },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove $cat", modifier = Modifier.size(14.dp))
                        }
                    }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newCategory,
                onValueChange = { newCategory = it },
                label = { Text("New Category") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(
                onClick = {
                    val trimmed = newCategory.trim()
                    if (trimmed.isNotBlank() && categories.none { it.equals(trimmed, ignoreCase = true) }) {
                        categories = categories + trimmed
                        newCategory = ""
                    }
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add category")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = budgetCap,
                onValueChange = { budgetCap = it.filter { c -> c.isDigit() } },
                label = { Text("Budget Under ₹") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = maxCap,
                onValueChange = { maxCap = it.filter { c -> c.isDigit() } },
                label = { Text("Max Slider Cap ₹") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        FilterToggle("Show Category Chips", enableCat) { enableCat = it }
        FilterToggle("Show Price Slider & Budget Chip", enablePrice) { enablePrice = it }
        FilterToggle("Show Combos Only Chip", enableCombo) { enableCombo = it }
        FilterToggle("Show In-Stock Chip", enableInStock) { enableInStock = it }
        FilterToggle("Show Sort Options", enableSort) { enableSort = it }

        Button(
            onClick = {
                onSave(
                    storeConfig.copy(
                        customCategoriesCsv = categories.joinToString(","),
                        enableCategoryFilter = enableCat,
                        enablePriceFilter = enablePrice,
                        enableComboFilter = enableCombo,
                        enableInStockFilter = enableInStock,
                        enableSortFilter = enableSort,
                        budgetPriceThresholdInr = budgetCap.toIntOrNull()?.coerceAtLeast(100) ?: 599,
                        maxPriceFilterCapInr = maxCap.toIntOrNull()?.coerceAtLeast(500) ?: 2999
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Search & Filter Settings")
        }
    }
}

@Composable
private fun PaymentsAndSecuritySection(
    storeConfig: StoreConfig,
    onSave: (String, StoreConfig) -> Unit
) {
    var phone by remember(storeConfig.merchantPhone) { mutableStateOf(storeConfig.merchantPhone) }
    var upiId by remember(storeConfig.upiId) { mutableStateOf(storeConfig.upiId) }
    var payeeName by remember(storeConfig.upiPayeeName) { mutableStateOf(storeConfig.upiPayeeName) }
    var codPercent by remember(storeConfig.codUpfrontPercent) { mutableStateOf(storeConfig.codUpfrontPercent.toString()) }
    var checkoutNotice by remember(storeConfig.checkoutNotice) { mutableStateOf(storeConfig.checkoutNotice) }
    var enableCod by remember(storeConfig.enableCodOption) { mutableStateOf(storeConfig.enableCodOption) }
    var enableFullUpi by remember(storeConfig.enableFullUpiOption) { mutableStateOf(storeConfig.enableFullUpiOption) }
    var newPassword by remember { mutableStateOf("") }

    SectionCard(title = "Payments, Contact & Admin Password", icon = Icons.Filled.Security) {
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it.filter { c -> c.isDigit() }.take(12) },
            label = { Text("Store Contact Phone") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = upiId,
            onValueChange = { upiId = it },
            label = { Text("Merchant UPI ID") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = payeeName,
            onValueChange = { payeeName = it },
            label = { Text("UPI Payee Display Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = codPercent,
            onValueChange = { codPercent = it.filter { c -> c.isDigit() }.take(3) },
            label = { Text("COD Mandatory Upfront % (Default 65)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = checkoutNotice,
            onValueChange = { checkoutNotice = it },
            label = { Text("COD Checkout Notice") },
            modifier = Modifier.fillMaxWidth()
        )
        FilterToggle("Enable Full UPI Checkout", enableFullUpi) { enableFullUpi = it }
        FilterToggle("Enable COD (Upfront % via UPI)", enableCod) { enableCod = it }

        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("Change Admin Password (leave blank to keep current)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val safeUpfront = codPercent.toIntOrNull()?.coerceIn(10, 100) ?: 65
                val updatedPassword = if (newPassword.trim().length >= 4) newPassword.trim() else storeConfig.adminPassword
                onSave(
                    if (newPassword.trim().length >= 4) "Payment & new Admin password saved!" else "Payment settings saved!",
                    storeConfig.copy(
                        merchantPhone = phone.trim().ifBlank { "7799248919" },
                        upiId = upiId.trim().ifBlank { "7799248919-m0b1@ybl" },
                        upiPayeeName = payeeName.trim().ifBlank { "MK Bags" },
                        codUpfrontPercent = safeUpfront,
                        checkoutNotice = checkoutNotice.trim(),
                        enableFullUpiOption = enableFullUpi || !enableCod,
                        enableCodOption = enableCod,
                        adminPassword = updatedPassword
                    )
                )
                newPassword = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Payment & Security Settings")
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            content()
        }
    }
}

@Composable
private fun FilterToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
