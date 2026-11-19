package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.OrderStatus

@Composable
fun FulfillmentTimeline(
    currentStatus: OrderStatus,
    modifier: Modifier = Modifier
) {
    if (currentStatus == OrderStatus.CANCELLED) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.errorContainer,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Cancel,
                    contentDescription = "Order Cancelled",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Column {
                    Text(
                        text = "Order Cancelled",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Contact MK Bags support if you need help with a refund or replacement.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }
        return
    }

    val steps = OrderStatus.activeTimelineSteps
    Column(modifier = modifier.fillMaxWidth()) {
        steps.forEachIndexed { index, step ->
            val isCompleted = currentStatus.stepIndex > step.stepIndex
            val isCurrent = currentStatus == step
            val dotColor = when {
                isCompleted -> MaterialTheme.colorScheme.tertiary
                isCurrent -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(dotColor),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isCompleted -> Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.size(14.dp)
                            )
                            isCurrent -> Icon(
                                imageVector = if (step == OrderStatus.SHIPPED || step == OrderStatus.OUT_FOR_DELIVERY) {
                                    Icons.Filled.LocalShipping
                                } else {
                                    Icons.Filled.Schedule
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    if (index < steps.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(22.dp)
                                .background(
                                    if (isCompleted) MaterialTheme.colorScheme.tertiary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                )
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.padding(bottom = if (index < steps.lastIndex) 10.dp else 0.dp)) {
                    Text(
                        text = step.label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isCurrent || isCompleted) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = if (isCurrent || isCompleted) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    )
                    if (isCurrent) {
                        Text(
                            text = step.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
