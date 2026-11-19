package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.data.model.ColorModeOption
import com.example.data.model.CornerStyleOption
import com.example.data.model.StoreConfig

@Composable
fun MkBagsTheme(
    storeConfig: StoreConfig = StoreConfig(),
    content: @Composable () -> Unit
) {
    val darkTheme = when (storeConfig.colorMode) {
        ColorModeOption.SYSTEM -> isSystemInDarkTheme()
        ColorModeOption.LIGHT -> false
        ColorModeOption.DARK -> true
    }

    val colorScheme = colorSchemeFor(storeConfig.themePreset, darkTheme)
    val typography = typographyFor(storeConfig.fontStyle)
    val radius = storeConfig.cornerStyle.cardRadiusDp
    val shapes = Shapes(
        extraSmall = RoundedCornerShape((radius / 3).coerceAtLeast(4).dp),
        small = RoundedCornerShape((radius / 2).coerceAtLeast(6).dp),
        medium = RoundedCornerShape(radius.dp),
        large = RoundedCornerShape((radius + 4).dp),
        extraLarge = RoundedCornerShape((radius + 10).dp)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes,
        content = content
    )
}
