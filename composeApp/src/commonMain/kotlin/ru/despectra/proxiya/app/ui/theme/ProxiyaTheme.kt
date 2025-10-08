package ru.despectra.proxiya.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import ru.despectra.proxiya.app.data.settings.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1B6756),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF39816E),
    onPrimaryContainer = Color(0xFFF4FFF9),
    secondary = Color(0xFF6A556A),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF836E83),
    onSecondaryContainer = Color(0xFFFFFBFF),
    tertiary = Color(0xFF436163),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF5B7A7C),
    onTertiaryContainer = Color(0xFFF4FFFF),
    error = Color(0xFFB41C1B),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFD83831),
    onErrorContainer = Color(0xFFFFFBFF),
    background = Color(0xFFF7FAF7),
    onBackground = Color(0xFF191C1B),
    surface = Color(0xFFFCF8F8),
    onSurface = Color(0xFF1C1B1B),
    surfaceVariant = Color(0xFFE1E3E5),
    onSurfaceVariant = Color(0xFF44474A),
    outline = Color(0xFF74777A),
    inverseOnSurface = Color(0xFFF4F0EF),
    inverseSurface = Color(0xFF313030),
    inversePrimary = Color(0xFF8DD4BF),
    surfaceTint = Color(0xFF1B6756),
    outlineVariant = Color(0xFFC4C7C9),
    scrim = Color(0xFF000000),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8DD4BF),
    onPrimary = Color(0xFF00382C),
    primaryContainer = Color(0xFF579D8A),
    onPrimaryContainer = Color(0xFF001711),
    secondary = Color(0xFFD8BFD7),
    onSecondary = Color(0xFF3C2B3D),
    secondaryContainer = Color(0xFFA18AA0),
    onSecondaryContainer = Color(0xFF1A0B1C),
    tertiary = Color(0xFFACCCCE),
    onTertiary = Color(0xFF163537),
    tertiaryContainer = Color(0xFF779698),
    onTertiaryContainer = Color(0xFF001E20),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFFFF5449),
    onErrorContainer = Color(0xFF5C0004),
    background = Color(0xFF101413),
    onBackground = Color(0xFFE0E3E0),
    surface = Color(0xFF141313),
    onSurface = Color(0xFFE5E2E1),
    surfaceVariant = Color(0xFF44474A),
    onSurfaceVariant = Color(0xFFC4C7C9),
    outline = Color(0xFF8E9194),
    inverseOnSurface = Color(0xFF313030),
    inverseSurface = Color(0xFFE5E2E1),
    inversePrimary = Color(0xFF1E6A58),
    surfaceTint = Color(0xFF8DD4BF),
    outlineVariant = Color(0xFF44474A),
    scrim = Color(0xFF000000),
)

@Composable
fun ProxiyaTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDarkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    val customColorScheme = if (isDarkTheme) {
        DarkCustomColorScheme
    } else {
        LightCustomColorScheme
    }

    CompositionLocalProvider(
        LocalCustomColors provides customColorScheme,
        LocalCodeTypography provides CodeTypographyConfig
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
