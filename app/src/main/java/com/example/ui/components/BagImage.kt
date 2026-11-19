package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.R
import java.io.File

@Composable
fun BagImage(
    imageKey: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    when {
        imageKey.startsWith("file:") -> {
            val file = File(imageKey.removePrefix("file:"))
            if (file.exists()) {
                AsyncImage(
                    model = file,
                    contentDescription = contentDescription,
                    modifier = modifier,
                    contentScale = contentScale
                )
            } else {
                FallbackBagPlaceholder(modifier)
            }
        }
        imageKey.startsWith("res:") -> {
            val resId = when (imageKey.removePrefix("res:")) {
                "img_hero_promo" -> R.drawable.img_hero_promo
                "img_bag_tote" -> R.drawable.img_bag_tote
                "img_bag_combo" -> R.drawable.img_bag_combo
                "img_bag_backpack" -> R.drawable.img_bag_backpack
                else -> R.drawable.img_bag_tote
            }
            Image(
                painter = painterResource(id = resId),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
        else -> FallbackBagPlaceholder(modifier)
    }
}

@Composable
private fun FallbackBagPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.ShoppingBag,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}
