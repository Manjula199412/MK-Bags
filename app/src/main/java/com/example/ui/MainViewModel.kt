package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DealNotificationHelper
import com.example.data.local.ImageStorageHelper
import com.example.data.local.MkBagsDatabase
import com.example.data.local.MkBagsRepository
import com.example.data.local.StoreConfigRepository
import com.example.data.model.CartItemWithProduct
import com.example.data.model.CustomerOrder
import com.example.data.model.DealAlert
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMode
import com.example.data.model.Product
import com.example.data.model.StoreConfig
import com.example.data.remote.GoogleSheetsOrderSyncClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CustomerTab {
    SHOP, SEARCH, CART, ORDERS, SETTINGS
}

enum class AdminTab {
    ADMIN_ORDERS, INVENTORY, CUSTOMIZE
}

enum class SortOption(val label: String) {
    FEATURED("Featured"),
    PRICE_LOW_HIGH("Price: Low to High"),
    PRICE_HIGH_LOW("Price: High to Low"),
    BIGGEST_SAVINGS("Biggest Savings")
}

data class SearchFilterState(
    val query: String = "",
    val selectedCategory: String? = null,
    val maxPriceInr: Int? = null,
    val onlyCombos: Boolean = false,
    val onlyBudgetDeals: Boolean = false,
    val onlyInStock: Boolean = false,
    val sortOption: SortOption = SortOption.FEATURED
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = MkBagsDatabase.getInstance(application)
    private val syncClient = GoogleSheetsOrderSyncClient(application)
    private val repository = MkBagsRepository(database, syncClient)
    private val configRepository = StoreConfigRepository(application)

    val storeConfig: StateFlow<StoreConfig> = configRepository.storeConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), StoreConfig())

    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val activeProducts: StateFlow<List<Product>> = repository.activeProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val cartItems: StateFlow<List<CartItemWithProduct>> = repository.cartItemsWithProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val allOrders: StateFlow<List<CustomerOrder>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val customerOrders: StateFlow<List<CustomerOrder>> = repository.customerOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val dealAlerts: StateFlow<List<DealAlert>> = repository.dealAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _customerTab = MutableStateFlow(CustomerTab.SHOP)
    val customerTab: StateFlow<CustomerTab> = _customerTab.asStateFlow()

    private val _adminTab = MutableStateFlow(AdminTab.ADMIN_ORDERS)
    val adminTab: StateFlow<AdminTab> = _adminTab.asStateFlow()

    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    private val _searchState = MutableStateFlow(SearchFilterState())
    val searchState: StateFlow<SearchFilterState> = _searchState.asStateFlow()

    private val _pendingCartScreenshotKey = MutableStateFlow("")
    val pendingCartScreenshotKey: StateFlow<String> = _pendingCartScreenshotKey.asStateFlow()

    private val _highlightedOrderId = MutableStateFlow<Long?>(null)
    val highlightedOrderId: StateFlow<Long?> = _highlightedOrderId.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val snackbarEvents = _snackbarEvents.asSharedFlow()

    val filteredSearchProducts: StateFlow<List<Product>> = combine(
        activeProducts,
        _searchState,
        storeConfig
    ) { products, filter, config ->
        val q = filter.query.trim().lowercase()
        val filtered = products.filter { p ->
            val matchesQuery = q.isEmpty() ||
                p.name.lowercase().contains(q) ||
                p.description.lowercase().contains(q) ||
                p.category.lowercase().contains(q) ||
                p.badgeText.lowercase().contains(q)

            val matchesCategory = filter.selectedCategory == null ||
                p.category.equals(filter.selectedCategory, ignoreCase = true)

            val matchesPrice = filter.maxPriceInr == null ||
                p.priceInr <= filter.maxPriceInr

            val matchesCombo = !filter.onlyCombos || p.isCombo
            val matchesBudget = !filter.onlyBudgetDeals ||
                (p.isBudgetDeal || p.priceInr <= config.budgetPriceThresholdInr)
            val matchesStock = !filter.onlyInStock || p.stock > 0

            matchesQuery && matchesCategory && matchesPrice && matchesCombo && matchesBudget && matchesStock
        }

        when (filter.sortOption) {
            SortOption.FEATURED -> filtered
            SortOption.PRICE_LOW_HIGH -> filtered.sortedBy { it.priceInr }
            SortOption.PRICE_HIGH_LOW -> filtered.sortedByDescending { it.priceInr }
            SortOption.BIGGEST_SAVINGS -> filtered.sortedByDescending { it.discountPercent }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    init {
        viewModelScope.launch {
            while (isActive) {
                repository.syncOrders(includeAllRemoteOrders = _isAdminMode.value)
                delay(8_000L)
            }
        }
    }

    fun selectCustomerTab(tab: CustomerTab) {
        _customerTab.value = tab
        if (tab == CustomerTab.ORDERS) {
            triggerOrderSync(includeAllRemote = false)
        }
    }

    fun selectAdminTab(tab: AdminTab) {
        _adminTab.value = tab
        if (tab == AdminTab.ADMIN_ORDERS) {
            triggerOrderSync(includeAllRemote = true)
        }
    }

    private fun triggerOrderSync(includeAllRemote: Boolean = _isAdminMode.value) {
        viewModelScope.launch {
            repository.syncOrders(includeAllRemoteOrders = includeAllRemote)
        }
    }

    fun openSearchWithFilter(
        category: String? = null,
        onlyCombos: Boolean = false,
        onlyBudgetDeals: Boolean = false
    ) {
        _searchState.value = SearchFilterState(
            selectedCategory = category,
            onlyCombos = onlyCombos,
            onlyBudgetDeals = onlyBudgetDeals
        )
        _customerTab.value = CustomerTab.SEARCH
    }

    fun updateSearchQuery(query: String) {
        _searchState.update { it.copy(query = query) }
    }

    fun updateSearchCategory(category: String?) {
        _searchState.update { it.copy(selectedCategory = category) }
    }

    fun updateMaxPriceFilter(maxPrice: Int?) {
        _searchState.update { it.copy(maxPriceInr = maxPrice) }
    }

    fun toggleComboFilter() {
        _searchState.update { it.copy(onlyCombos = !it.onlyCombos) }
    }

    fun toggleBudgetFilter() {
        _searchState.update { it.copy(onlyBudgetDeals = !it.onlyBudgetDeals) }
    }

    fun toggleInStockFilter() {
        _searchState.update { it.copy(onlyInStock = !it.onlyInStock) }
    }

    fun updateSortOption(option: SortOption) {
        _searchState.update { it.copy(sortOption = option) }
    }

    fun resetSearchFilters() {
        _searchState.value = SearchFilterState()
    }

    fun addToCart(product: Product, quantity: Int = 1) {
        viewModelScope.launch {
            val added = repository.addToCart(product, quantity)
            if (added) {
                _snackbarEvents.tryEmit("Added ${product.name} to cart")
            } else {
                _snackbarEvents.tryEmit("${product.name} is currently out of stock")
            }
        }
    }

    fun updateCartQuantity(productId: Long, quantity: Int, maxStock: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(productId, quantity, maxStock)
        }
    }

    fun removeFromCart(productId: Long) {
        viewModelScope.launch {
            repository.removeFromCart(productId)
        }
    }

    fun setCartScreenshotUri(uri: Uri) {
        viewModelScope.launch {
            val savedKey = ImageStorageHelper.persistPickedImage(getApplication(), uri, "cart_pay_ss")
            if (savedKey != null) {
                _pendingCartScreenshotKey.value = savedKey
                _snackbarEvents.tryEmit("Payment screenshot attached")
            } else {
                _snackbarEvents.tryEmit("Could not read selected screenshot")
            }
        }
    }

    fun clearCartScreenshot() {
        _pendingCartScreenshotKey.value = ""
    }

    fun attachScreenshotToOrder(orderId: Long, uri: Uri) {
        viewModelScope.launch {
            val savedKey = ImageStorageHelper.persistPickedImage(getApplication(), uri, "order_pay_ss")
            if (savedKey != null) {
                repository.attachScreenshotToOrder(orderId, savedKey)
                _highlightedOrderId.value = orderId
                _snackbarEvents.tryEmit("Payment screenshot saved to order")
                triggerOrderSync(includeAllRemote = _isAdminMode.value)
            } else {
                _snackbarEvents.tryEmit("Could not load selected screenshot")
            }
        }
    }

    fun handleSharedScreenshotUri(uri: Uri) {
        viewModelScope.launch {
            val savedKey = ImageStorageHelper.persistPickedImage(getApplication(), uri, "shared_pay_ss")
            if (savedKey == null) {
                _snackbarEvents.tryEmit("Could not read shared screenshot. Please attach it inside the app.")
                return@launch
            }
            _isAdminMode.value = false
            val recentOrder = repository.attachScreenshotToRecentOrder(savedKey)
            if (recentOrder != null) {
                _highlightedOrderId.value = recentOrder.id
                _customerTab.value = CustomerTab.ORDERS
                _snackbarEvents.tryEmit("Screenshot attached to recent order ${recentOrder.orderNumber}!")
                triggerOrderSync(includeAllRemote = false)
            } else {
                _pendingCartScreenshotKey.value = savedKey
                _customerTab.value = CustomerTab.CART
                _snackbarEvents.tryEmit("No recent order yet — screenshot attached to Cart checkout!")
            }
        }
    }

    fun placeOrder(
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        paymentMode: PaymentMode,
        upiTransactionRef: String,
        onSuccess: (CustomerOrder) -> Unit
    ) {
        viewModelScope.launch {
            val currentCart = cartItems.value
            val cfg = storeConfig.value
            val screenshotKey = _pendingCartScreenshotKey.value
            val normalizedRef = upiTransactionRef.trim().ifBlank {
                if (screenshotKey.isNotBlank()) "Screenshot Attached" else "Screenshot Pending"
            }
            val result = repository.placeOrder(
                customerName = customerName,
                customerPhone = customerPhone,
                deliveryAddress = deliveryAddress,
                cartItems = currentCart,
                paymentMode = paymentMode,
                codUpfrontPercent = cfg.codUpfrontPercent,
                upiTransactionRef = normalizedRef,
                paymentScreenshotKey = screenshotKey
            )
            result.onSuccess { order ->
                _pendingCartScreenshotKey.value = ""
                _highlightedOrderId.value = order.id
                configRepository.updateStoreConfig {
                    it.copy(
                        customerName = customerName.trim(),
                        customerPhone = customerPhone.trim(),
                        customerAddress = deliveryAddress.trim()
                    )
                }
                _snackbarEvents.tryEmit("Order ${order.orderNumber} placed! Track it live in Orders.")
                _customerTab.value = CustomerTab.ORDERS
                triggerOrderSync(includeAllRemote = false)
                onSuccess(order)
            }.onFailure { err ->
                _snackbarEvents.tryEmit(err.message ?: "Could not place order")
            }
        }
    }

    fun saveCustomerProfile(name: String, phone: String, address: String, dealAlertsEnabled: Boolean) {
        viewModelScope.launch {
            configRepository.updateStoreConfig {
                it.copy(
                    customerName = name.trim(),
                    customerPhone = phone.trim(),
                    customerAddress = address.trim(),
                    customerDealAlertsEnabled = dealAlertsEnabled
                )
            }
            _snackbarEvents.tryEmit("Customer preferences saved")
        }
    }

    fun verifyAndUnlockAdmin(enteredPassword: String): Boolean {
        val valid = enteredPassword == storeConfig.value.adminPassword
        if (valid) {
            _isAdminMode.value = true
            _adminTab.value = AdminTab.ADMIN_ORDERS
            triggerOrderSync(includeAllRemote = true)
        }
        return valid
    }

    fun exitAdminMode() {
        _isAdminMode.value = false
    }

    fun updateOrderStatus(orderId: Long, status: OrderStatus, adminNote: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status, adminNote)
            _snackbarEvents.tryEmit("Order updated to ${status.label}")
            triggerOrderSync(includeAllRemote = true)
        }
    }

    fun saveProduct(product: Product) {
        viewModelScope.launch {
            repository.saveProduct(product)
            _snackbarEvents.tryEmit(if (product.id == 0L) "Added ${product.name}" else "Updated ${product.name}")
        }
    }

    fun adjustStock(productId: Long, delta: Int) {
        viewModelScope.launch {
            repository.adjustProductStock(productId, delta)
        }
    }

    fun deleteProduct(productId: Long) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
            _snackbarEvents.tryEmit("Product removed")
        }
    }

    fun publishDealAlert(title: String, message: String, promoCode: String) {
        viewModelScope.launch {
            val alert = repository.publishDealAlert(title, message, promoCode)
            val cfg = storeConfig.value
            val notified = if (cfg.customerDealAlertsEnabled) {
                DealNotificationHelper.showDealNotification(
                    context = getApplication(),
                    title = alert.title,
                    message = alert.message,
                    promoCode = alert.promoCode
                )
            } else false
            _snackbarEvents.tryEmit(
                if (notified) "Deal alert published & notification sent!"
                else "Deal alert published to storefront!"
            )
        }
    }

    fun deleteDealAlert(alert: DealAlert) {
        viewModelScope.launch {
            repository.deleteDealAlert(alert)
        }
    }

    fun updateStoreConfig(message: String? = null, transform: (StoreConfig) -> StoreConfig) {
        viewModelScope.launch {
            configRepository.updateStoreConfig(transform)
            if (message != null) {
                _snackbarEvents.tryEmit(message)
            }
        }
    }

    fun importPickedPhoto(uri: Uri, prefix: String, onSaved: (String) -> Unit) {
        viewModelScope.launch {
            val savedKey = ImageStorageHelper.persistPickedImage(getApplication(), uri, prefix)
            if (savedKey != null) {
                onSaved(savedKey)
                _snackbarEvents.tryEmit("Photo updated")
            } else {
                _snackbarEvents.tryEmit("Could not load selected photo")
            }
        }
    }
}
