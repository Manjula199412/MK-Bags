package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.DealAlert
import com.example.data.model.Product
import com.example.data.model.StoreConfig
import com.example.ui.components.BagImage
import com.example.ui.components.ProductCard
import com.example.ui.util.formatTimestamp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    storeConfig: StoreConfig,
    products: List<Product>,
    dealAlerts: List<DealAlert>,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onExploreCombos: () -> Unit,
    onExploreBudget: () -> Unit,
    onSelectCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAlertsSheet by remember { mutableStateOf(false) }

    val comboProducts = remember(products) { products.filter { it.isCombo } }
    val budgetProducts = remember(products, storeConfig.budgetPriceThresholdInr) {
        products.filter { it.isBudgetDeal || it.priceInr <= storeConfig.budgetPriceThresholdInr }
    }

    var remainingSeconds by remember(storeConfig.heroCountdownHours) {
        mutableLongStateOf(storeConfig.heroCountdownHours.coerceAtLeast(1) * 3600L)
    }
    LaunchedEffect(storeConfig.heroCountdownHours) {
        remainingSeconds = storeConfig.heroCountdownHours.coerceAtLeast(1) * 3600L
        while (remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds = (remainingSeconds - 1).coerceAtLeast(0L)
        }
    }

    val hours = remainingSeconds / 3600
    val minutes = (remainingSeconds % 3600) / 60
    val seconds = remainingSeconds % 60
    val timerText = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Storefront Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = storeConfig.storeName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = storeConfig.storeTagline,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { showAlertsSheet = true },
                    modifier = Modifier.testTag("deal_alerts_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (dealAlerts.isNotEmpty()) {
                                Badge { Text(dealAlerts.size.toString()) }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = "View deal alerts",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Limited-Time Promotional Hero Banner
        if (storeConfig.showPromoSection) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(216.dp)
                        .clickable { onExploreCombos() }
                        .testTag("promo_hero_banner"),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        BagImage(
                            imageKey = storeConfig.heroBannerImageKey,
                            contentDescription = storeConfig.heroBannerTitle,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.78f),
                                            Color.Black.copy(alpha = 0.42f),
                                            Color.Black.copy(alpha = 0.12f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondary,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = storeConfig.heroBannerBadge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    color = Color.Black.copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Timer,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = timerText,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Column(modifier = Modifier.fillMaxWidth(0.82f)) {
                                Text(
                                    text = storeConfig.heroBannerTitle,
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = storeConfig.heroBannerSubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(10.dp))
                                Button(
                                    onClick = onExploreCombos,
                                    shape = MaterialTheme.shapes.small,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary
                                    ),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Shop Deals", style = MaterialTheme.typography.labelMedium)
                                    Spacer(Modifier.size(4.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Category Filter Row
        if (storeConfig.categoriesList.isNotEmpty()) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(storeConfig.categoriesList) { category ->
                        FilterChip(
                            selected = false,
                            onClick = { onSelectCategory(category) },
                            label = { Text(category, style = MaterialTheme.typography.labelMedium) }
                        )
                    }
                }
            }
        }

        // Bundle & Combo Deals Section
        if (comboProducts.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader(
                        title = storeConfig.promoSectionTitle,
                        actionLabel = "View all",
                        onAction = onExploreCombos
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(comboProducts, key = { "combo_${it.id}" }) { product ->
                            ProductCard(
                                product = product,
                                onProductClick = onProductClick,
                                onAddToCart = onAddToCart,
                                modifier = Modifier.width(184.dp)
                            )
                        }
                    }
                }
            }
        }

        // Budget / Cheap Picks Section
        if (budgetProducts.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader(
                        title = "${storeConfig.budgetSectionTitle} (Under ₹${storeConfig.budgetPriceThresholdInr})",
                        actionLabel = "See cheap bags",
                        onAction = onExploreBudget
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(budgetProducts, key = { "budget_${it.id}" }) { product ->
                            ProductCard(
                                product = product,
                                onProductClick = onProductClick,
                                onAddToCart = onAddToCart,
                                modifier = Modifier.width(176.dp)
                            )
                        }
                    }
                }
            }
        }

        // All Bags Grid
        item {
            SectionHeader(
                title = storeConfig.allBagsSectionTitle,
                actionLabel = "${products.size} items",
                onAction = {}
            )
        }

        val chunkedProducts = products.chunked(2)
        items(chunkedProducts.size) { rowIndex ->
            val rowItems = chunkedProducts[rowIndex]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProductCard(
                    product = rowItems[0],
                    onProductClick = onProductClick,
                    onAddToCart = onAddToCart,
                    modifier = Modifier.weight(1f)
                )
                if (rowItems.size > 1) {
                    ProductCard(
                        product = rowItems[1],
                        onProductClick = onProductClick,
                        onAddToCart = onAddToCart,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }

    if (showAlertsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAlertsSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalOffer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Deal Alerts & Limited Offers",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                if (dealAlerts.isEmpty()) {
                    Text(
                        text = "No new alerts right now. Check back soon for combo drops!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    dealAlerts.forEach { alert ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = alert.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = formatTimestamp(alert.createdAt),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = alert.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (alert.promoCode.isNotBlank()) {
                                    Spacer(Modifier.height(6.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "CODE: ${alert.promoCode}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        TextButton(onClick = onAction) {
            Text(actionLabel, style = MaterialTheme.typography.labelMedium)
        }
    }
}
