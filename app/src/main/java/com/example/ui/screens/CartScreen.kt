package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.CartItemWithProduct
import com.example.data.model.PaymentMode
import com.example.data.model.StoreConfig
import com.example.ui.components.BagImage
import com.example.ui.util.formatInr
import kotlin.math.roundToInt

@Composable
fun CartScreen(
    storeConfig: StoreConfig,
    cartItems: List<CartItemWithProduct>,
    pendingScreenshotKey: String = "",
    onAttachScreenshotUri: (Uri) -> Unit = {},
    onClearScreenshot: () -> Unit = {},
    onUpdateQuantity: (Long, Int, Int) -> Unit,
    onRemoveItem: (Long) -> Unit,
    onBrowseShop: () -> Unit,
    onPlaceOrder: (
        name: String,
        phone: String,
        address: String,
        paymentMode: PaymentMode,
        upiRef: String
    ) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val screenshotPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { onAttachScreenshotUri(it) }
    }

    var customerName by remember { mutableStateOf(storeConfig.customerName) }
    var customerPhone by remember { mutableStateOf(storeConfig.customerPhone) }
    var deliveryAddress by remember { mutableStateOf(storeConfig.customerAddress) }
    var selectedPaymentMode by remember(storeConfig.enableFullUpiOption, storeConfig.enableCodOption) {
        mutableStateOf(
            if (storeConfig.enableFullUpiOption) PaymentMode.UPI_FULL else PaymentMode.COD_65_UPFRONT
        )
    }
    var upiRef by remember { mutableStateOf("") }
    var willShareScreenshotAfter by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(pendingScreenshotKey) {
        if (pendingScreenshotKey.isNotBlank()) {
            validationError = null
        }
    }

    LaunchedEffect(storeConfig.customerName, storeConfig.customerPhone, storeConfig.customerAddress) {
        if (customerName.isBlank() && storeConfig.customerName.isNotBlank()) customerName = storeConfig.customerName
        if (customerPhone.isBlank() && storeConfig.customerPhone.isNotBlank()) customerPhone = storeConfig.customerPhone
        if (deliveryAddress.isBlank() && storeConfig.customerAddress.isNotBlank()) deliveryAddress = storeConfig.customerAddress
    }

    if (cartItems.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.ShoppingBag,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Your bag cart is empty",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Explore limited-time combos, budget picks, and everyday totes to start an order.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onBrowseShop,
                modifier = Modifier.testTag("empty_cart_browse_button")
            ) {
                Text("Explore Bags")
            }
        }
        return
    }

    val totalAmount = cartItems.sumOf { it.lineTotalInr }
    val codPercent = storeConfig.codUpfrontPercent.coerceIn(10, 100)
    val payableNow = when (selectedPaymentMode) {
        PaymentMode.UPI_FULL -> totalAmount
        PaymentMode.COD_65_UPFRONT -> ((totalAmount * (codPercent / 100.0)).roundToInt()).toDouble()
    }
    val balanceDueOnDelivery = (totalAmount - payableNow).coerceAtLeast(0.0)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Your Cart (${cartItems.sumOf { it.quantity }} items)",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        // Cart Items
        items(cartItems, key = { it.product.id }) { item ->
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
                        imageKey = item.product.imageKey,
                        contentDescription = item.product.name,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(MaterialTheme.shapes.small)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.product.name,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "${formatInr(item.product.priceInr)} each",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { onUpdateQuantity(item.product.id, item.quantity - 1, item.product.stock) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Filled.Remove, contentDescription = "Decrease quantity")
                            }
                            Text(
                                text = item.quantity.toString(),
                                style = MaterialTheme.typography.titleSmall
                            )
                            IconButton(
                                onClick = { onUpdateQuantity(item.product.id, item.quantity + 1, item.product.stock) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Increase quantity")
                            }
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        IconButton(onClick = { onRemoveItem(item.product.id) }) {
                            Icon(
                                Icons.Filled.DeleteOutline,
                                contentDescription = "Remove item",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        Text(
                            text = formatInr(item.lineTotalInr),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Delivery Details Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Delivery Details", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it; validationError = null },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_name_input")
                    )
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it.filter { c -> c.isDigit() }.take(10); validationError = null },
                        label = { Text("Mobile Number (10 digits)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_phone_input")
                    )
                    OutlinedTextField(
                        value = deliveryAddress,
                        onValueChange = { deliveryAddress = it; validationError = null },
                        label = { Text("Full Delivery Address & Pincode") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_address_input")
                    )
                }
            }
        }

        // Payment Method & UPI / 65% COD Upfront Box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Payment Method", style = MaterialTheme.typography.titleMedium)

                    if (storeConfig.enableFullUpiOption) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedPaymentMode == PaymentMode.UPI_FULL,
                                onClick = { selectedPaymentMode = PaymentMode.UPI_FULL }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pay Full via UPI (100%)", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "Pay ${formatInr(totalAmount)} now via UPI • Zero balance on delivery",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (storeConfig.enableCodOption) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedPaymentMode == PaymentMode.COD_65_UPFRONT,
                                onClick = { selectedPaymentMode = PaymentMode.COD_65_UPFRONT }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Cash on Delivery ($codPercent% Upfront Required)",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    storeConfig.checkoutNotice,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    // Payment Summary Breakdown
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Order Total", style = MaterialTheme.typography.bodyMedium)
                        Text(formatInr(totalAmount), style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = if (selectedPaymentMode == PaymentMode.UPI_FULL) "Payable Now (100% UPI)"
                            else "Payable Now ($codPercent% Upfront via UPI)",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = formatInr(payableNow),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (balanceDueOnDelivery > 0) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = "Balance on Delivery (${100 - codPercent}% COD)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatInr(balanceDueOnDelivery),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Interactive UPI Payment Box
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Pay ${formatInr(payableNow)} to ${storeConfig.upiPayeeName}",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("UPI ID", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = storeConfig.upiId,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        clipboard.setText(AnnotatedString(storeConfig.upiId))
                                        onShowMessage("UPI ID ${storeConfig.upiId} copied!")
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.ContentCopy,
                                        contentDescription = "Copy UPI ID",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.size(6.dp))
                                    Text("Copy UPI")
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val amountStr = String.format("%.2f", payableNow)
                                        val upiUri = Uri.parse("upi://pay").buildUpon()
                                            .appendQueryParameter("pa", storeConfig.upiId)
                                            .appendQueryParameter("pn", storeConfig.upiPayeeName)
                                            .appendQueryParameter("am", amountStr)
                                            .appendQueryParameter("cu", "INR")
                                            .appendQueryParameter("tn", "Order at ${storeConfig.storeName}")
                                            .build()
                                        val intent = Intent(Intent.ACTION_VIEW, upiUri)
                                        try {
                                            context.startActivity(Intent.createChooser(intent, "Pay ${formatInr(payableNow)} via UPI"))
                                        } catch (_: Exception) {
                                            clipboard.setText(AnnotatedString(storeConfig.upiId))
                                            onShowMessage("No UPI app detected. UPI ID copied — pay ${formatInr(payableNow)} and enter ref below.")
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("launch_upi_app_button")
                                ) {
                                    Icon(Icons.Filled.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.size(6.dp))
                                    Text("Pay via UPI App")
                                }

                                OutlinedButton(
                                    onClick = {
                                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${storeConfig.merchantPhone}"))
                                        try {
                                            context.startActivity(dialIntent)
                                        } catch (_: Exception) {
                                            onShowMessage("Call MK Bags at ${storeConfig.merchantPhone}")
                                        }
                                    }
                                ) {
                                    Icon(Icons.Filled.Call, contentDescription = "Call Store", modifier = Modifier.size(18.dp))
                                }
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            )

                            Text(
                                text = "Payment Proof (No UTR number needed!)",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = "Upload a payment screenshot below, or tap Share on your screenshot from any UPI app / Gallery and choose MK Bags — it links directly to your recent order.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (pendingScreenshotKey.isNotBlank()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        BagImage(
                                            imageKey = pendingScreenshotKey,
                                            contentDescription = "Attached payment screenshot",
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.CheckCircle,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "Screenshot Attached",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            TextButton(
                                                onClick = {
                                                    screenshotPicker.launch(
                                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                    )
                                                },
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text("Change Screenshot")
                                            }
                                        }
                                        IconButton(
                                            onClick = onClearScreenshot,
                                            modifier = Modifier.testTag("remove_cart_screenshot_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Close,
                                                contentDescription = "Remove attached screenshot"
                                            )
                                        }
                                    }
                                }
                            } else {
                                FilledTonalButton(
                                    onClick = {
                                        screenshotPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("upload_cart_screenshot_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AddPhotoAlternate,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.size(8.dp))
                                    Text("Upload Payment Screenshot")
                                }
                            }

                            FilterChip(
                                selected = willShareScreenshotAfter,
                                onClick = {
                                    willShareScreenshotAfter = !willShareScreenshotAfter
                                    validationError = null
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Share,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = { Text("I'll share screenshot right after placing order") },
                                modifier = Modifier.testTag("share_screenshot_after_chip")
                            )

                            OutlinedTextField(
                                value = upiRef,
                                onValueChange = { upiRef = it; validationError = null },
                                label = { Text("Payer Name or UPI Ref (Optional if screenshot added)") },
                                placeholder = { Text("e.g. Your name, GPay/PhonePe ref, or UTR") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("checkout_upi_ref_input")
                            )
                        }
                    }

                    validationError?.let { errorMsg ->
                        Text(
                            text = errorMsg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Button(
                        onClick = {
                            val hasPaymentProof = pendingScreenshotKey.isNotBlank() ||
                                willShareScreenshotAfter ||
                                upiRef.trim().length >= 2
                            when {
                                customerName.isBlank() -> validationError = "Please enter your full name."
                                customerPhone.length < 10 -> validationError = "Please enter a valid 10-digit mobile number."
                                deliveryAddress.isBlank() -> validationError = "Please enter your delivery address."
                                !hasPaymentProof -> validationError = "Upload a screenshot, choose 'share screenshot after order', or enter payer name / ref."
                                else -> onPlaceOrder(
                                    customerName,
                                    customerPhone,
                                    deliveryAddress,
                                    selectedPaymentMode,
                                    upiRef
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("place_order_button"),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = if (willShareScreenshotAfter && pendingScreenshotKey.isBlank()) {
                                "Place Order • Share Screenshot Next (${formatInr(payableNow)})"
                            } else {
                                "Confirm & Place Order • ${formatInr(payableNow)} Paid"
                            },
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
