package com.example.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.Product
import com.example.data.model.StoreConfig
import com.example.ui.SearchFilterState
import com.example.ui.SortOption
import com.example.ui.components.ProductCard
import kotlin.math.roundToInt

@Composable
fun SearchScreen(
    storeConfig: StoreConfig,
    searchState: SearchFilterState,
    filteredProducts: List<Product>,
    onQueryChange: (String) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onMaxPriceChange: (Int?) -> Unit,
    onToggleCombo: () -> Unit,
    onToggleBudget: () -> Unit,
    onToggleInStock: () -> Unit,
    onSortChange: (SortOption) -> Unit,
    onResetFilters: () -> Unit,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPricePanel by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        // Search Input Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = searchState.query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input_field"),
                placeholder = { Text("Search totes, combos, slings, backpacks...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (searchState.query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear search")
                            }
                        }
                        if (storeConfig.enablePriceFilter) {
                            IconButton(
                                onClick = { showPricePanel = !showPricePanel },
                                modifier = Modifier.testTag("toggle_price_filter_button")
                            ) {
                                Icon(
                                    Icons.Filled.Tune,
                                    contentDescription = "Toggle price filter",
                                    tint = if (searchState.maxPriceInr != null) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            // Expandable Max Price Filter Slider (customizable cap from Admin)
            if (showPricePanel && storeConfig.enablePriceFilter) {
                val cap = storeConfig.maxPriceFilterCapInr.coerceAtLeast(500).toFloat()
                val currentSlider = (searchState.maxPriceInr ?: storeConfig.maxPriceFilterCapInr).toFloat().coerceIn(200f, cap)
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (searchState.maxPriceInr == null) "Max Price: Any (up to ₹${cap.toInt()})"
                                else "Max Price: ₹${searchState.maxPriceInr}",
                                style = MaterialTheme.typography.titleSmall
                            )
                            TextButton(onClick = { onMaxPriceChange(null) }) {
                                Text("Any")
                            }
                        }
                        Slider(
                            value = currentSlider,
                            onValueChange = { onMaxPriceChange(it.roundToInt()) },
                            valueRange = 200f..cap
                        )
                    }
                }
            }

            // Customizable Categories Filter Row
            if (storeConfig.enableCategoryFilter && storeConfig.categoriesList.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = searchState.selectedCategory == null,
                            onClick = { onCategorySelected(null) },
                            label = { Text("All") }
                        )
                    }
                    items(storeConfig.categoriesList) { category ->
                        FilterChip(
                            selected = searchState.selectedCategory.equals(category, ignoreCase = true),
                            onClick = {
                                onCategorySelected(
                                    if (searchState.selectedCategory.equals(category, ignoreCase = true)) null else category
                                )
                            },
                            label = { Text(category) }
                        )
                    }
                }
            }

            // Customizable Attribute & Sort Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (storeConfig.enableComboFilter) {
                    item {
                        FilterChip(
                            selected = searchState.onlyCombos,
                            onClick = onToggleCombo,
                            label = { Text("Combos Only") }
                        )
                    }
                }
                if (storeConfig.enablePriceFilter) {
                    item {
                        FilterChip(
                            selected = searchState.onlyBudgetDeals,
                            onClick = onToggleBudget,
                            label = { Text("Under ₹${storeConfig.budgetPriceThresholdInr}") }
                        )
                    }
                }
                if (storeConfig.enableInStockFilter) {
                    item {
                        FilterChip(
                            selected = searchState.onlyInStock,
                            onClick = onToggleInStock,
                            label = { Text("In Stock") }
                        )
                    }
                }
                if (storeConfig.enableSortFilter) {
                    items(SortOption.entries) { option ->
                        FilterChip(
                            selected = searchState.sortOption == option,
                            onClick = { onSortChange(option) },
                            label = { Text(option.label) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredProducts.size} bags found",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (searchState != SearchFilterState()) {
                    TextButton(onClick = onResetFilters) {
                        Text("Reset filters")
                    }
                }
            }
        }

        if (filteredProducts.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "No matching bags found",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Try clearing filters or searching another style.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = onResetFilters) {
                    Text("Reset All Filters")
                }
            }
        } else {
            val rows = filteredProducts.chunked(2)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(rows.size) { rowIndex ->
                    val rowItems = rows[rowIndex]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
        }
    }
}
