package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CustomerOrder
import com.example.data.model.OrderStatus
import com.example.data.model.StoreConfig
import com.example.ui.components.BagImage
import com.example.ui.components.FulfillmentTimeline
import com.example.ui.util.formatInr
import com.example.ui.util.formatTimestamp

@Composable
fun OrdersScreen(
    storeConfig: StoreConfig,
    orders: List<CustomerOrder>,
    highlightedOrderId: Long? = null,
    onAttachOrderScreenshot: (orderId: Long, uri: Uri) -> Unit = { _, _ -> },
    onBrowseShop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var pendingUploadOrderId by remember { mutableStateOf<Long?>(null) }
    var zoomedScreenshotKey by remember { mutableStateOf<String?>(null) }

    val screenshotPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val targetOrderId = pendingUploadOrderId
        if (uri != null && targetOrderId != null) {
            onAttachOrderScreenshot(targetOrderId, uri)
        }
        pendingUploadOrderId = null
    }

    LaunchedEffect(highlightedOrderId, orders.size) {
        if (highlightedOrderId != null && orders.isNotEmpty()) {
            val idx = orders.indexOfFirst { it.id == highlightedOrderId }
            if (idx >= 0) {
                listState.animateScrollToItem(idx + 1)
            }
        }
    }

    if (orders.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.ReceiptLong,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(14.dp))
            Text("No orders placed yet", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Once you place an order from your cart, you can track every step from confirmation to delivery right here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))
            Button(onClick = onBrowseShop) {
                Text("Shop Bags")
            }
        }
        return
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("Track Your Orders", style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = "Live manual status updates from ${storeConfig.storeName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(orders, key = { it.id }) { order ->
            val lineItems = order.parseLineItems()
            val status = order.orderStatus
            val isHighlighted = order.id == highlightedOrderId

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("customer_order_card_${order.id}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = if (isHighlighted) {
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                } else {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = order.orderNumber,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = formatTimestamp(order.createdAt),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = when (status) {
                                OrderStatus.DELIVERED -> MaterialTheme.colorScheme.tertiaryContainer
                                OrderStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.primaryContainer
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = status.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = when (status) {
                                    OrderStatus.DELIVERED -> MaterialTheme.colorScheme.onTertiaryContainer
                                    OrderStatus.CANCELLED -> MaterialTheme.colorScheme.onErrorContainer
                                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // Live Admin Update Note
                    if (order.adminNote.isNotBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocalShipping,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = order.adminNote,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Visual Step-by-Step Fulfillment Timeline
                    FulfillmentTimeline(currentStatus = status)

                    HorizontalDivider()

                    // Items Summary
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        lineItems.forEach { line ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${line.quantity}× ${line.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = formatInr(line.lineTotalInr),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    // Payment & Delivery Info
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Payment Mode", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(order.paymentModeEnum.label, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Paid Upfront (${order.upiTransactionRef})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                            Text(formatInr(order.upfrontPaidInr), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                        }
                        if (order.balanceDueOnDeliveryInr > 0) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Balance Due on Delivery", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                Text(formatInr(order.balanceDueOnDeliveryInr), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                        Text(
                            text = "Deliver to: ${order.customerName} (${order.customerPhone}) • ${order.deliveryAddress}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Payment Screenshot Section
                    Surface(
                        color = if (order.paymentScreenshotKey.isNotBlank()) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (order.paymentScreenshotKey.isNotBlank()) {
                                        "Payment Screenshot Attached"
                                    } else {
                                        "Payment Screenshot Needed"
                                    },
                                    style = MaterialTheme.typography.labelLarge
                                )
                                TextButton(
                                    onClick = {
                                        pendingUploadOrderId = order.id
                                        screenshotPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier.testTag("order_upload_screenshot_${order.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AddPhotoAlternate,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.size(6.dp))
                                    Text(if (order.paymentScreenshotKey.isNotBlank()) "Replace" else "Upload")
                                }
                            }

                            if (order.paymentScreenshotKey.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { zoomedScreenshotKey = order.paymentScreenshotKey }
                                        .testTag("order_screenshot_thumb_${order.id}")
                                ) {
                                    BagImage(
                                        imageKey = order.paymentScreenshotKey,
                                        contentDescription = "Order ${order.orderNumber} payment screenshot",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.55f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.ZoomIn,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "Tap to view",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = "No UTR needed — tap Upload above or share your UPI screenshot from Gallery to MK Bags to attach it directly here.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                FilledTonalButton(
                                    onClick = {
                                        pendingUploadOrderId = order.id
                                        screenshotPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.size(8.dp))
                                    Text("Attach Payment Screenshot")
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${storeConfig.merchantPhone}"))
                            try {
                                context.startActivity(dialIntent)
                            } catch (_: Exception) {
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("Call Store Support (${storeConfig.merchantPhone})")
                    }
                }
            }
        }
    }

    zoomedScreenshotKey?.let { key ->
        ScreenshotZoomDialog(
            imageKey = key,
            onDismiss = { zoomedScreenshotKey = null }
        )
    }
}

@Composable
private fun ScreenshotZoomDialog(
    imageKey: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
                .clickable(onClick = onDismiss)
                .padding(16.dp)
        ) {
            BagImage(
                imageKey = imageKey,
                contentDescription = "Full payment screenshot",
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                contentScale = ContentScale.Fit
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close screenshot preview",
                    tint = Color.White
                )
            }
        }
    }
}
