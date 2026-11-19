package com.example.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.DealAlert
import com.example.data.model.Product
import com.example.data.model.StoreConfig
import com.example.ui.components.BagImage
import com.example.ui.util.PRESET_IMAGE_OPTIONS
import com.example.ui.util.formatInr

@Composable
fun AdminInventoryTab(
    storeConfig: StoreConfig,
    products: List<Product>,
    dealAlerts: List<DealAlert>,
    onSaveProduct: (Product) -> Unit,
    onAdjustStock: (Long, Int) -> Unit,
    onDeleteProduct: (Long) -> Unit,
    onPublishDealAlert: (title: String, message: String, promoCode: String) -> Unit,
    onDeleteDealAlert: (DealAlert) -> Unit,
    onImportPickedPhoto: (Uri, String, (String) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var showProductSheet by remember { mutableStateOf(false) }

    var alertTitle by remember { mutableStateOf("") }
    var alertMessage by remember { mutableStateOf("") }
    var alertCode by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Push Deal Alert Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Text("Push Deal Alert Notification", style = MaterialTheme.typography.titleMedium)
                    }
                    OutlinedTextField(
                        value = alertTitle,
                        onValueChange = { alertTitle = it },
                        label = { Text("Alert Headline (e.g. Flash 3-Bag Combo ₹999!)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = alertMessage,
                        onValueChange = { alertMessage = it },
                        label = { Text("Offer Details") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = alertCode,
                        onValueChange = { alertCode = it.uppercase() },
                        label = { Text("Promo Code (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (alertTitle.isNotBlank() && alertMessage.isNotBlank()) {
                                onPublishDealAlert(alertTitle, alertMessage, alertCode)
                                alertTitle = ""
                                alertMessage = ""
                                alertCode = ""
                            }
                        },
                        enabled = alertTitle.isNotBlank() && alertMessage.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("publish_deal_alert_button")
                    ) {
                        Text("Send Deal Alert Now")
                    }

                    if (dealAlerts.isNotEmpty()) {
                        Text("Active Alerts (${dealAlerts.size})", style = MaterialTheme.typography.labelMedium)
                        dealAlerts.take(3).forEach { alert ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "• ${alert.title}",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { onDeleteDealAlert(alert) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete alert", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Catalog Inventory Header + Add Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Bags & Combos (${products.size})", style = MaterialTheme.typography.titleLarge)
                Button(
                    onClick = {
                        editingProduct = Product(
                            name = "",
                            description = "",
                            category = storeConfig.categoriesList.firstOrNull() ?: "Totes",
                            priceInr = 499.0,
                            originalPriceInr = 799.0,
                            stock = 10,
                            imageKey = "res:img_bag_tote"
                        )
                        showProductSheet = true
                    },
                    modifier = Modifier.testTag("admin_add_product_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Add Bag")
                }
            }
        }

        items(products, key = { it.id }) { product ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BagImage(
                        imageKey = product.imageKey,
                        contentDescription = product.name,
                        modifier = Modifier
                            .size(68.dp)
                            .clip(MaterialTheme.shapes.small)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.name, style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = "${formatInr(product.priceInr)} (MRP ${formatInr(product.originalPriceInr)}) • ${product.category}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = { onAdjustStock(product.id, -1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Filled.Remove, contentDescription = "Decrease stock", modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = "Stock: ${product.stock}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (product.stock == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = { onAdjustStock(product.id, 1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Increase stock", modifier = Modifier.size(16.dp))
                            }
                            if (!product.isActive) {
                                Text("• HIDDEN", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    Column {
                        IconButton(onClick = {
                            editingProduct = product
                            showProductSheet = true
                        }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit product")
                        }
                        IconButton(onClick = { onDeleteProduct(product.id) }) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete product", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showProductSheet && editingProduct != null) {
        ProductEditorBottomSheet(
            initialProduct = editingProduct!!,
            categories = storeConfig.categoriesList.ifEmpty { listOf("Totes", "Combos", "Backpacks", "Slings") },
            onDismiss = { showProductSheet = false },
            onSave = { updated ->
                onSaveProduct(updated)
                showProductSheet = false
            },
            onImportPickedPhoto = onImportPickedPhoto
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductEditorBottomSheet(
    initialProduct: Product,
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit,
    onImportPickedPhoto: (Uri, String, (String) -> Unit) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(initialProduct.name) }
    var description by remember { mutableStateOf(initialProduct.description) }
    var category by remember { mutableStateOf(initialProduct.category) }
    var priceText by remember { mutableStateOf(initialProduct.priceInr.toInt().toString()) }
    var originalPriceText by remember { mutableStateOf(initialProduct.originalPriceInr.toInt().toString()) }
    var stockText by remember { mutableStateOf(initialProduct.stock.toString()) }
    var badgeText by remember { mutableStateOf(initialProduct.badgeText) }
    var imageKey by remember { mutableStateOf(initialProduct.imageKey) }
    var isCombo by remember { mutableStateOf(initialProduct.isCombo) }
    var isBudgetDeal by remember { mutableStateOf(initialProduct.isBudgetDeal) }
    var isLimitedOffer by remember { mutableStateOf(initialProduct.isLimitedOffer) }
    var isActive by remember { mutableStateOf(initialProduct.isActive) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onImportPickedPhoto(uri, "prod") { savedKey ->
                imageKey = savedKey
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (initialProduct.id == 0L) "Add New Bag / Combo" else "Edit Bag Details",
                style = MaterialTheme.typography.titleLarge
            )

            // Editable Product Photo Picker + Studio Presets
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BagImage(
                    imageKey = imageKey,
                    contentDescription = "Product preview",
                    modifier = Modifier
                        .size(88.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Pick Photo from Gallery")
                    }
                    Text("Or choose a studio photo below:", style = MaterialTheme.typography.labelSmall)
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PRESET_IMAGE_OPTIONS) { (key, label) ->
                    FilterChip(
                        selected = imageKey == key,
                        onClick = { imageKey = key },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Bag / Combo Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description & Included Items") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Category", style = MaterialTheme.typography.labelMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = category.equals(cat, ignoreCase = true),
                        onClick = { category = cat },
                        label = { Text(cat) }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { c -> c.isDigit() } },
                    label = { Text("Selling ₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = originalPriceText,
                    onValueChange = { originalPriceText = it.filter { c -> c.isDigit() } },
                    label = { Text("MRP ₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = stockText,
                    onValueChange = { stockText = it.filter { c -> c.isDigit() } },
                    label = { Text("Stock") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(0.8f)
                )
            }

            OutlinedTextField(
                value = badgeText,
                onValueChange = { badgeText = it },
                label = { Text("Badge Label (e.g. 3-PC COMBO, UNDER ₹400)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            ToggleRow("Combo / Bundle Deal", isCombo) { isCombo = it }
            ToggleRow("Budget / Cheap Pick", isBudgetDeal) { isBudgetDeal = it }
            ToggleRow("Highlight in Limited Offers", isLimitedOffer) { isLimitedOffer = it }
            ToggleRow("Visible in Customer Storefront", isActive) { isActive = it }

            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull()?.coerceAtLeast(1.0) ?: 1.0
                    val mrp = originalPriceText.toDoubleOrNull()?.coerceAtLeast(price) ?: price
                    val stock = stockText.toIntOrNull()?.coerceAtLeast(0) ?: 0
                    onSave(
                        initialProduct.copy(
                            name = name.trim().ifBlank { "MK Bag" },
                            description = description.trim(),
                            category = category,
                            priceInr = price,
                            originalPriceInr = mrp,
                            stock = stock,
                            imageKey = imageKey,
                            isCombo = isCombo,
                            isBudgetDeal = isBudgetDeal,
                            isLimitedOffer = isLimitedOffer,
                            isActive = isActive,
                            badgeText = badgeText.trim()
                        )
                    )
                },
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Save Product")
            }
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
