package com.example.ui.screens.admin

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.components.BagImage
import com.example.ui.util.formatInr
import com.example.ui.util.formatTimestamp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminOrdersTab(
    orders: List<CustomerOrder>,
    onUpdateOrderStatus: (orderId: Long, status: OrderStatus, adminNote: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf<OrderStatus?>(null) }
    var zoomedScreenshotKey by remember { mutableStateOf<String?>(null) }

    val filteredOrders = remember(orders, selectedFilter) {
        if (selectedFilter == null) orders
        else orders.filter { it.orderStatus == selectedFilter }
    }

    val pendingCount = orders.count { it.orderStatus == OrderStatus.PENDING_CONFIRMATION }
    val totalRevenue = orders.filter { it.orderStatus != OrderStatus.CANCELLED }.sumOf { it.totalAmountInr }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary KPIs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryStatCard(
                    label = "Total Orders",
                    value = orders.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    label = "Pending",
                    value = pendingCount.toString(),
                    highlight = pendingCount > 0,
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    label = "Sales",
                    value = formatInr(totalRevenue),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Status Filter Row
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("All (${orders.size})") }
                    )
                }
                items(OrderStatus.entries) { status ->
                    val count = orders.count { it.orderStatus == status }
                    FilterChip(
                        selected = selectedFilter == status,
                        onClick = { selectedFilter = if (selectedFilter == status) null else status },
                        label = { Text("${status.label} ($count)") }
                    )
                }
            }
        }

        if (filteredOrders.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Inbox,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("No orders in this filter", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Customer orders placed from Cart appear here in real time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredOrders, key = { it.id }) { order ->
                AdminOrderCard(
                    order = order,
                    onCallCustomer = {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.customerPhone}"))
                        try {
                            context.startActivity(dialIntent)
                        } catch (_: Exception) {
                        }
                    },
                    onUpdateStatus = { newStatus, note ->
                        onUpdateOrderStatus(order.id, newStatus, note)
                    },
                    onInspectScreenshot = { key ->
                        zoomedScreenshotKey = key
                    }
                )
            }
        }
    }

    zoomedScreenshotKey?.let { key ->
        Dialog(
            onDismissRequest = { zoomedScreenshotKey = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f))
                    .clickable { zoomedScreenshotKey = null }
                    .padding(16.dp)
            ) {
                BagImage(
                    imageKey = key,
                    contentDescription = "Customer payment screenshot preview",
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { zoomedScreenshotKey = null },
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
}

@Composable
private fun SummaryStatCard(
    label: String,
    value: String,
    highlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminOrderCard(
    order: CustomerOrder,
    onCallCustomer: () -> Unit,
    onUpdateStatus: (OrderStatus, String) -> Unit,
    onInspectScreenshot: (String) -> Unit
) {
    var noteInput by remember(order.id, order.adminNote) { mutableStateOf(order.adminNote) }
    val lineItems = remember(order.itemsJson) { order.parseLineItems() }
    val currentStatus = order.orderStatus

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_order_card_${order.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${order.orderNumber} • ${order.customerName}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${formatTimestamp(order.createdAt)} • ${order.customerPhone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onCallCustomer) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = "Call customer",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = "Address: ${order.deliveryAddress}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    lineItems.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.quantity}× ${item.name}", style = MaterialTheme.typography.bodySmall)
                            Text(formatInr(item.lineTotalInr), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total (${order.paymentModeEnum.label})", style = MaterialTheme.typography.labelMedium)
                        Text(formatInr(order.totalAmountInr), style = MaterialTheme.typography.labelLarge)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Upfront Paid • Ref: ${order.upiTransactionRef}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                        Text(formatInr(order.upfrontPaidInr), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                    }
                    if (order.balanceDueOnDeliveryInr > 0) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Collect on Delivery", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                            Text(formatInr(order.balanceDueOnDeliveryInr), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }

            if (order.paymentScreenshotKey.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onInspectScreenshot(order.paymentScreenshotKey) }
                        .testTag("admin_order_screenshot_${order.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BagImage(
                            imageKey = order.paymentScreenshotKey,
                            contentDescription = "Customer payment screenshot",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Customer Payment Screenshot",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = "Tap to zoom and verify payment",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.ZoomIn,
                            contentDescription = "Zoom payment screenshot",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else if (order.upiTransactionRef.contains("Pending", ignoreCase = true)) {
                Text(
                    text = "Awaiting customer payment screenshot",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick Confirm CTA for Pending Orders
            if (currentStatus == OrderStatus.PENDING_CONFIRMATION) {
                Button(
                    onClick = {
                        onUpdateStatus(OrderStatus.CONFIRMED, "Payment verified & order confirmed by MK Bags.")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_confirm_order_${order.id}")
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Verify Payment & Confirm Order")
                }
            }

            Text("Manual Fulfillment Status", style = MaterialTheme.typography.labelMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OrderStatus.entries.forEach { status ->
                    FilterChip(
                        selected = currentStatus == status,
                        onClick = {
                            onUpdateStatus(status, noteInput.ifBlank { status.description })
                        },
                        label = { Text(status.label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("Live Customer Tracking Note") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = { onUpdateStatus(currentStatus, noteInput) },
                    modifier = Modifier.height(52.dp)
                ) {
                    Text("Save")
                }
            }
        }
    }
}
