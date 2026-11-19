package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.content.IntentCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.DealNotificationHelper
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.ui.AdminTab
import com.example.ui.CustomerTab
import com.example.ui.MainViewModel
import com.example.ui.components.ProductDetailSheet
import com.example.ui.screens.CartScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.screens.admin.AdminCustomizeTab
import com.example.ui.screens.admin.AdminInventoryTab
import com.example.ui.screens.admin.AdminOrdersTab
import com.example.ui.theme.MkBagsTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        DealNotificationHelper.ensureChannel(this)
        handleIncomingShareIntent(intent)
        setContent {
            MkBagsApp(viewModel = mainViewModel)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingShareIntent(intent)
    }

    private fun handleIncomingShareIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true) {
            val sharedUri = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
            if (sharedUri != null) {
                mainViewModel.handleSharedScreenshotUri(sharedUri)
                intent.action = null
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MkBagsApp(viewModel: MainViewModel = viewModel()) {
    val storeConfig by viewModel.storeConfig.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val activeProducts by viewModel.activeProducts.collectAsStateWithLifecycle()
    val filteredProducts by viewModel.filteredSearchProducts.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val orders by viewModel.allOrders.collectAsStateWithLifecycle()
    val customerOrders by viewModel.customerOrders.collectAsStateWithLifecycle()
    val dealAlerts by viewModel.dealAlerts.collectAsStateWithLifecycle()

    val customerTab by viewModel.customerTab.collectAsStateWithLifecycle()
    val adminTab by viewModel.adminTab.collectAsStateWithLifecycle()
    val isAdminMode by viewModel.isAdminMode.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val pendingCartScreenshotKey by viewModel.pendingCartScreenshotKey.collectAsStateWithLifecycle()
    val highlightedOrderId by viewModel.highlightedOrderId.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvents.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Predictive & clean back handling
    BackHandler(enabled = isAdminMode || customerTab != CustomerTab.SHOP) {
        if (isAdminMode) {
            viewModel.exitAdminMode()
        } else {
            viewModel.selectCustomerTab(CustomerTab.SHOP)
        }
    }

    val cartBadgeCount = cartItems.sumOf { it.quantity }
    val activeOrdersCount = customerOrders.count {
        it.orderStatus != OrderStatus.DELIVERED && it.orderStatus != OrderStatus.CANCELLED
    }
    val pendingAdminOrdersCount = orders.count { it.orderStatus == OrderStatus.PENDING_CONFIRMATION }

    MkBagsTheme(storeConfig = storeConfig) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                if (isAdminMode) {
                    TopAppBar(
                        title = {
                            Text(
                                text = when (adminTab) {
                                    AdminTab.ADMIN_ORDERS -> "Admin-Orders"
                                    AdminTab.INVENTORY -> "Inventory & Alerts"
                                    AdminTab.CUSTOMIZE -> "Customize ${storeConfig.storeName}"
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        actions = {
                            TextButton(
                                onClick = { viewModel.exitAdminMode() },
                                modifier = Modifier.testTag("exit_admin_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                                Text(" ${stringResource(R.string.admin_exit)}")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            },
            bottomBar = {
                if (isAdminMode) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = adminTab == AdminTab.ADMIN_ORDERS,
                            onClick = { viewModel.selectAdminTab(AdminTab.ADMIN_ORDERS) },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (pendingAdminOrdersCount > 0) {
                                            Badge { Text(pendingAdminOrdersCount.toString()) }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (adminTab == AdminTab.ADMIN_ORDERS) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                                        contentDescription = stringResource(R.string.nav_admin_orders)
                                    )
                                }
                            },
                            label = { Text(stringResource(R.string.nav_admin_orders), maxLines = 1) },
                            modifier = Modifier.testTag("tab_admin_orders")
                        )
                        NavigationBarItem(
                            selected = adminTab == AdminTab.INVENTORY,
                            onClick = { viewModel.selectAdminTab(AdminTab.INVENTORY) },
                            icon = {
                                Icon(
                                    imageVector = if (adminTab == AdminTab.INVENTORY) Icons.Filled.Inventory2 else Icons.Outlined.Inventory2,
                                    contentDescription = stringResource(R.string.nav_admin_inventory)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_admin_inventory), maxLines = 1) },
                            modifier = Modifier.testTag("tab_admin_inventory")
                        )
                        NavigationBarItem(
                            selected = adminTab == AdminTab.CUSTOMIZE,
                            onClick = { viewModel.selectAdminTab(AdminTab.CUSTOMIZE) },
                            icon = {
                                Icon(
                                    imageVector = if (adminTab == AdminTab.CUSTOMIZE) Icons.Filled.Palette else Icons.Outlined.Palette,
                                    contentDescription = stringResource(R.string.nav_admin_customize)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_admin_customize), maxLines = 1) },
                            modifier = Modifier.testTag("tab_admin_customize")
                        )
                    }
                } else {
                    NavigationBar {
                        NavigationBarItem(
                            selected = customerTab == CustomerTab.SHOP,
                            onClick = { viewModel.selectCustomerTab(CustomerTab.SHOP) },
                            icon = {
                                Icon(
                                    imageVector = if (customerTab == CustomerTab.SHOP) Icons.Filled.ShoppingBag else Icons.Outlined.ShoppingBag,
                                    contentDescription = stringResource(R.string.nav_shop)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_shop), maxLines = 1) },
                            modifier = Modifier.testTag("tab_shop")
                        )
                        NavigationBarItem(
                            selected = customerTab == CustomerTab.SEARCH,
                            onClick = { viewModel.selectCustomerTab(CustomerTab.SEARCH) },
                            icon = {
                                Icon(
                                    imageVector = if (customerTab == CustomerTab.SEARCH) Icons.Filled.Search else Icons.Outlined.Search,
                                    contentDescription = stringResource(R.string.nav_search)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_search), maxLines = 1) },
                            modifier = Modifier.testTag("tab_search")
                        )
                        NavigationBarItem(
                            selected = customerTab == CustomerTab.CART,
                            onClick = { viewModel.selectCustomerTab(CustomerTab.CART) },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (cartBadgeCount > 0) {
                                            Badge { Text(cartBadgeCount.toString()) }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (customerTab == CustomerTab.CART) Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart,
                                        contentDescription = stringResource(R.string.nav_cart)
                                    )
                                }
                            },
                            label = { Text(stringResource(R.string.nav_cart), maxLines = 1) },
                            modifier = Modifier.testTag("tab_cart")
                        )
                        NavigationBarItem(
                            selected = customerTab == CustomerTab.ORDERS,
                            onClick = { viewModel.selectCustomerTab(CustomerTab.ORDERS) },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (activeOrdersCount > 0) {
                                            Badge { Text(activeOrdersCount.toString()) }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (customerTab == CustomerTab.ORDERS) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                                        contentDescription = stringResource(R.string.nav_orders)
                                    )
                                }
                            },
                            label = { Text(stringResource(R.string.nav_orders), maxLines = 1) },
                            modifier = Modifier.testTag("tab_orders")
                        )
                        NavigationBarItem(
                            selected = customerTab == CustomerTab.SETTINGS,
                            onClick = { viewModel.selectCustomerTab(CustomerTab.SETTINGS) },
                            icon = {
                                Icon(
                                    imageVector = if (customerTab == CustomerTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                    contentDescription = stringResource(R.string.nav_settings)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_settings), maxLines = 1) },
                            modifier = Modifier.testTag("tab_settings")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isAdminMode) {
                    when (adminTab) {
                        AdminTab.ADMIN_ORDERS -> AdminOrdersTab(
                            orders = orders,
                            onUpdateOrderStatus = viewModel::updateOrderStatus
                        )
                        AdminTab.INVENTORY -> AdminInventoryTab(
                            storeConfig = storeConfig,
                            products = allProducts,
                            dealAlerts = dealAlerts,
                            onSaveProduct = viewModel::saveProduct,
                            onAdjustStock = viewModel::adjustStock,
                            onDeleteProduct = viewModel::deleteProduct,
                            onPublishDealAlert = viewModel::publishDealAlert,
                            onDeleteDealAlert = viewModel::deleteDealAlert,
                            onImportPickedPhoto = viewModel::importPickedPhoto
                        )
                        AdminTab.CUSTOMIZE -> AdminCustomizeTab(
                            storeConfig = storeConfig,
                            onUpdateStoreConfig = viewModel::updateStoreConfig,
                            onImportPickedPhoto = viewModel::importPickedPhoto
                        )
                    }
                } else {
                    when (customerTab) {
                        CustomerTab.SHOP -> ShopScreen(
                            storeConfig = storeConfig,
                            products = activeProducts,
                            dealAlerts = dealAlerts,
                            onProductClick = { selectedProduct = it },
                            onAddToCart = { viewModel.addToCart(it, 1) },
                            onExploreCombos = { viewModel.openSearchWithFilter(onlyCombos = true) },
                            onExploreBudget = { viewModel.openSearchWithFilter(onlyBudgetDeals = true) },
                            onSelectCategory = { cat -> viewModel.openSearchWithFilter(category = cat) }
                        )
                        CustomerTab.SEARCH -> SearchScreen(
                            storeConfig = storeConfig,
                            searchState = searchState,
                            filteredProducts = filteredProducts,
                            onQueryChange = viewModel::updateSearchQuery,
                            onCategorySelected = viewModel::updateSearchCategory,
                            onMaxPriceChange = viewModel::updateMaxPriceFilter,
                            onToggleCombo = viewModel::toggleComboFilter,
                            onToggleBudget = viewModel::toggleBudgetFilter,
                            onToggleInStock = viewModel::toggleInStockFilter,
                            onSortChange = viewModel::updateSortOption,
                            onResetFilters = viewModel::resetSearchFilters,
                            onProductClick = { selectedProduct = it },
                            onAddToCart = { viewModel.addToCart(it, 1) }
                        )
                        CustomerTab.CART -> CartScreen(
                            storeConfig = storeConfig,
                            cartItems = cartItems,
                            pendingScreenshotKey = pendingCartScreenshotKey,
                            onAttachScreenshotUri = viewModel::setCartScreenshotUri,
                            onClearScreenshot = viewModel::clearCartScreenshot,
                            onUpdateQuantity = viewModel::updateCartQuantity,
                            onRemoveItem = viewModel::removeFromCart,
                            onBrowseShop = { viewModel.selectCustomerTab(CustomerTab.SHOP) },
                            onPlaceOrder = { name, phone, address, mode, ref ->
                                viewModel.placeOrder(name, phone, address, mode, ref) {}
                            },
                            onShowMessage = { msg ->
                                scope.launch { snackbarHostState.showSnackbar(msg) }
                            }
                        )
                        CustomerTab.ORDERS -> OrdersScreen(
                            storeConfig = storeConfig,
                            orders = customerOrders,
                            highlightedOrderId = highlightedOrderId,
                            onAttachOrderScreenshot = viewModel::attachScreenshotToOrder,
                            onBrowseShop = { viewModel.selectCustomerTab(CustomerTab.SHOP) }
                        )
                        CustomerTab.SETTINGS -> SettingsScreen(
                            storeConfig = storeConfig,
                            dealAlerts = dealAlerts,
                            onSaveProfile = viewModel::saveCustomerProfile,
                            onUnlockAdmin = viewModel::verifyAndUnlockAdmin,
                            onShowMessage = { msg ->
                                scope.launch { snackbarHostState.showSnackbar(msg) }
                            }
                        )
                    }
                }
            }
        }

        selectedProduct?.let { product ->
            ProductDetailSheet(
                product = product,
                codUpfrontPercent = storeConfig.codUpfrontPercent,
                onDismiss = { selectedProduct = null },
                onAddToCart = { p, qty -> viewModel.addToCart(p, qty) }
            )
        }
    }
}
