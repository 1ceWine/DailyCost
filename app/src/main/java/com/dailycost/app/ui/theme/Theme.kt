package com.dailycost.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 主题配色：每套包含浅色 / 深色的主色与容器色。
 */
data class AccentColors(
    val label: String,
    val lightPrimary: Color,
    val lightContainer: Color,
    val lightOnContainer: Color,
    val darkPrimary: Color,
    val darkContainer: Color,
    val darkOnContainer: Color,
    val secondary: Color,
    val darkSecondary: Color
)

val AccentPalettes = listOf(
    AccentColors(
        "海蓝",
        Color(0xFF2F6FED), Color(0xFFDBE5FF), Color(0xFF001A41),
        Color(0xFFACC7FF), Color(0xFF17408A), Color(0xFFDBE5FF),
        Color(0xFF00696E), Color(0xFF4CD9E1)
    ),
    AccentColors(
        "松绿",
        Color(0xFF0F8A5F), Color(0xFFC8F2DD), Color(0xFF00201A),
        Color(0xFF63DCAE), Color(0xFF0B5B41), Color(0xFFC8F2DD),
        Color(0xFF7A5900), Color(0xFFF3BF30)
    ),
    AccentColors(
        "墨紫",
        Color(0xFF6E4BD8), Color(0xFFE8DEFF), Color(0xFF1E0A4B),
        Color(0xFFCFBEFF), Color(0xFF48308F), Color(0xFFE8DEFF),
        Color(0xFF9A4A00), Color(0xFFFFB77C)
    ),
    AccentColors(
        "暖橙",
        Color(0xFFD9622B), Color(0xFFFFDBCB), Color(0xFF3B0A00),
        Color(0xFFFFB59A), Color(0xFF8A3A12), Color(0xFFFFDBCB),
        Color(0xFF00696E), Color(0xFF4CD9E1)
    ),
    AccentColors(
        "莓红",
        Color(0xFFC2185B), Color(0xFFFFD9E2), Color(0xFF3F0018),
        Color(0xFFFFB1C6), Color(0xFF8C0A3E), Color(0xFFFFD9E2),
        Color(0xFF4B5A9E), Color(0xFFB3C1FF)
    )
)

@Composable
fun DailyCostTheme(
    darkMode: Int = 0,
    accent: Int = 0,
    content: @Composable () -> Unit
) {
    val dark = when (darkMode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
    val a = AccentPalettes[accent.coerceIn(0, AccentPalettes.lastIndex)]

    val scheme = if (dark) {
        darkColorScheme(
            primary = a.darkPrimary,
            onPrimary = Color(0xFF0A0F1C),
            primaryContainer = a.darkContainer,
            onPrimaryContainer = a.darkOnContainer,
            secondary = a.darkSecondary,
            secondaryContainer = Color(0xFF243044),
            onSecondaryContainer = Color(0xFFD7E3F5),
            tertiary = Color(0xFFF3BF30),
            background = Color(0xFF101418),
            surface = Color(0xFF15191E),
            surfaceVariant = Color(0xFF252B33),
            onSurfaceVariant = Color(0xFFC2C9D4),
            outline = Color(0xFF5A6270),
            error = Color(0xFFFFB4AB)
        )
    } else {
        lightColorScheme(
            primary = a.lightPrimary,
            onPrimary = Color.White,
            primaryContainer = a.lightContainer,
            onPrimaryContainer = a.lightOnContainer,
            secondary = a.secondary,
            secondaryContainer = Color(0xFFDDE7F6),
            onSecondaryContainer = Color(0xFF0E2138),
            tertiary = Color(0xFF7A5900),
            background = Color(0xFFF7F8FB),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFEDF0F5),
            onSurfaceVariant = Color(0xFF4A5262),
            outline = Color(0xFFB7BECB),
            error = Color(0xFFB3261E)
        )
    }

    MaterialTheme(colorScheme = scheme, content = content)
}
