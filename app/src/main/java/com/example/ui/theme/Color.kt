package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.example.data.model.ThemePreset

fun colorSchemeFor(preset: ThemePreset, darkTheme: Boolean): ColorScheme {
    return when (preset) {
        ThemePreset.ESPRESSO_LUXE -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFE5B885),
                onPrimary = Color(0xFF3E2414),
                primaryContainer = Color(0xFF5D3A24),
                onPrimaryContainer = Color(0xFFFFE5CA),
                secondary = Color(0xFFD6B99B),
                onSecondary = Color(0xFF3A2A1C),
                secondaryContainer = Color(0xFF52402F),
                onSecondaryContainer = Color(0xFFF3E0CD),
                tertiary = Color(0xFF9EC4A0),
                onTertiary = Color(0xFF133519),
                background = Color(0xFF18120E),
                onBackground = Color(0xFFF3EAE3),
                surface = Color(0xFF1E1713),
                onSurface = Color(0xFFF3EAE3),
                surfaceVariant = Color(0xFF332822),
                onSurfaceVariant = Color(0xFFD6C5BA),
                outline = Color(0xFF7B6B60),
                error = Color(0xFFFFB4AB),
                onError = Color(0xFF690005)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF4A2C20),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFF5DFCE),
                onPrimaryContainer = Color(0xFF2D160B),
                secondary = Color(0xFFC58940),
                onSecondary = Color(0xFFFFFFFF),
                secondaryContainer = Color(0xFFFFE9CD),
                onSecondaryContainer = Color(0xFF362004),
                tertiary = Color(0xFF4F6F52),
                onTertiary = Color(0xFFFFFFFF),
                background = Color(0xFFFCF8F5),
                onBackground = Color(0xFF211A16),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF211A16),
                surfaceVariant = Color(0xFFF3EAE3),
                onSurfaceVariant = Color(0xFF5A4C43),
                outline = Color(0xFFD5C4B9),
                error = Color(0xFFBA1A1A),
                onError = Color(0xFFFFFFFF)
            )
        }

        ThemePreset.TERRACOTTA_WARM -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFFFB59D),
                onPrimary = Color(0xFF5C1906),
                primaryContainer = Color(0xFF7D2D15),
                onPrimaryContainer = Color(0xFFFFDBD0),
                secondary = Color(0xFFE7C29E),
                onSecondary = Color(0xFF432C14),
                secondaryContainer = Color(0xFF5C4228),
                onSecondaryContainer = Color(0xFFFFE5CD),
                tertiary = Color(0xFFB4CFA5),
                background = Color(0xFF1A1210),
                onBackground = Color(0xFFF5E9E5),
                surface = Color(0xFF221815),
                onSurface = Color(0xFFF5E9E5),
                surfaceVariant = Color(0xFF3B2C27),
                onSurfaceVariant = Color(0xFFDCC3BA),
                outline = Color(0xFF886E65)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFFB84A27),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFFFDBD0),
                onPrimaryContainer = Color(0xFF3E1003),
                secondary = Color(0xFF7B5E3B),
                onSecondary = Color(0xFFFFFFFF),
                secondaryContainer = Color(0xFFF8E5CE),
                onSecondaryContainer = Color(0xFF2F1F0D),
                tertiary = Color(0xFF4C6A47),
                onTertiary = Color(0xFFFFFFFF),
                background = Color(0xFFFFF8F5),
                onBackground = Color(0xFF231916),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF231916),
                surfaceVariant = Color(0xFFF8EBE6),
                onSurfaceVariant = Color(0xFF5F4942),
                outline = Color(0xFFDDC5BC)
            )
        }

        ThemePreset.MIDNIGHT_INDIGO -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFBAC3FF),
                onPrimary = Color(0xFF16216B),
                primaryContainer = Color(0xFF293888),
                onPrimaryContainer = Color(0xFFDDE1FF),
                secondary = Color(0xFF76D8CE),
                onSecondary = Color(0xFF003733),
                secondaryContainer = Color(0xFF00504A),
                onSecondaryContainer = Color(0xFFA7F5EC),
                tertiary = Color(0xFFFFB784),
                background = Color(0xFF10131D),
                onBackground = Color(0xFFE6E9F4),
                surface = Color(0xFF161A27),
                onSurface = Color(0xFFE6E9F4),
                surfaceVariant = Color(0xFF272D3F),
                onSurfaceVariant = Color(0xFFC4C9DC),
                outline = Color(0xFF6B728A)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF283593),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFE0E5FF),
                onPrimaryContainer = Color(0xFF0B144F),
                secondary = Color(0xFF00796B),
                onSecondary = Color(0xFFFFFFFF),
                secondaryContainer = Color(0xFFCFF5EF),
                onSecondaryContainer = Color(0xFF002B26),
                tertiary = Color(0xFFD96B27),
                onTertiary = Color(0xFFFFFFFF),
                background = Color(0xFFF6F8FC),
                onBackground = Color(0xFF161A26),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF161A26),
                surfaceVariant = Color(0xFFEAEEF8),
                onSurfaceVariant = Color(0xFF474E63),
                outline = Color(0xFFC5CCE0)
            )
        }

        ThemePreset.MATCHA_MINIMAL -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFA7CF9D),
                onPrimary = Color(0xFF163814),
                primaryContainer = Color(0xFF2C5028),
                onPrimaryContainer = Color(0xFFC5ECC0),
                secondary = Color(0xFFDDBB96),
                onSecondary = Color(0xFF3E2811),
                secondaryContainer = Color(0xFF583F25),
                onSecondaryContainer = Color(0xFFFCE0C0),
                tertiary = Color(0xFF9AC9C6),
                background = Color(0xFF111610),
                onBackground = Color(0xFFE8EDE5),
                surface = Color(0xFF171D16),
                onSurface = Color(0xFFE8EDE5),
                surfaceVariant = Color(0xFF293227),
                onSurfaceVariant = Color(0xFFC3CEBF),
                outline = Color(0xFF6F7B6B)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF3E5C3A),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFDCEFD6),
                onPrimaryContainer = Color(0xFF152912),
                secondary = Color(0xFFA47148),
                onSecondary = Color(0xFFFFFFFF),
                secondaryContainer = Color(0xFFF7E4D1),
                onSecondaryContainer = Color(0xFF361F0B),
                tertiary = Color(0xFF3D6F6C),
                onTertiary = Color(0xFFFFFFFF),
                background = Color(0xFFF7FAF5),
                onBackground = Color(0xFF191E18),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF191E18),
                surfaceVariant = Color(0xFFEBF1E7),
                onSurfaceVariant = Color(0xFF485245),
                outline = Color(0xFFC6D1C1)
            )
        }

        ThemePreset.ROSE_VELVET -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFFFB0C9),
                onPrimary = Color(0xFF5B0D2D),
                primaryContainer = Color(0xFF7B2144),
                onPrimaryContainer = Color(0xFFFFD9E3),
                secondary = Color(0xFFF0C48D),
                onSecondary = Color(0xFF472C08),
                secondaryContainer = Color(0xFF63421B),
                onSecondaryContainer = Color(0xFFFFE4C4),
                tertiary = Color(0xFFB3CAE6),
                background = Color(0xFF1A1115),
                onBackground = Color(0xFFF5E8ED),
                surface = Color(0xFF22171C),
                onSurface = Color(0xFFF5E8ED),
                surfaceVariant = Color(0xFF392730),
                onSurfaceVariant = Color(0xFFDDC1CD),
                outline = Color(0xFF836773)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF8E244D),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFFFD9E4),
                onPrimaryContainer = Color(0xFF3B061B),
                secondary = Color(0xFFC78A3B),
                onSecondary = Color(0xFFFFFFFF),
                secondaryContainer = Color(0xFFFFE8CA),
                onSecondaryContainer = Color(0xFF382103),
                tertiary = Color(0xFF5B507A),
                onTertiary = Color(0xFFFFFFFF),
                background = Color(0xFFFFF6F9),
                onBackground = Color(0xFF23181D),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF23181D),
                surfaceVariant = Color(0xFFF8E9EF),
                onSurfaceVariant = Color(0xFF5D4650),
                outline = Color(0xFFDDC2CD)
            )
        }
    }
}
