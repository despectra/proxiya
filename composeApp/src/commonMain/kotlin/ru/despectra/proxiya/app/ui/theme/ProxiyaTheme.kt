package ru.despectra.proxiya.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import ru.despectra.proxiya.app.data.settings.ThemeMode


// ШАБЛОН ДЛЯ ПРОМПТОВ
/*private val LightColorScheme = lightColorScheme(
    primary = Color(_),
    onPrimary = Color(_),
    primaryContainer = Color(_),
    onPrimaryContainer = Color(_),
    secondary = Color(_),
    onSecondary = Color(_),
    secondaryContainer = Color(_),
    onSecondaryContainer = Color(_),
    tertiary = Color(_),
    onTertiary = Color(_),
    tertiaryContainer = Color(_),
    onTertiaryContainer = Color(_),
    error = Color(_),
    onError = Color(_),
    errorContainer = Color(_),
    onErrorContainer = Color(_),
    background = Color(_),
    onBackground = Color(_),
    surface = Color(_),
    onSurface = Color(_),
    surfaceVariant = Color(_),
    onSurfaceVariant = Color(_),
    outline = Color(_),
    inverseOnSurface = Color(_),
    inverseSurface = Color(_),
    inversePrimary = Color(_),
    surfaceTint = Color(_),
    outlineVariant = Color(_),
    scrim = Color(_),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(_),
    onPrimary = Color(_),
    primaryContainer = Color(_),
    onPrimaryContainer = Color(_),
    secondary = Color(_),
    onSecondary = Color(_),
    secondaryContainer = Color(_),
    onSecondaryContainer = Color(_),
    tertiary = Color(_),
    onTertiary = Color(_),
    tertiaryContainer = Color(_),
    onTertiaryContainer = Color(_),
    error = Color(_),
    onError = Color(_),
    errorContainer = Color(_),
    onErrorContainer = Color(_),
    background = Color(_),
    onBackground = Color(_),
    surface = Color(_),
    onSurface = Color(_),
    surfaceVariant = Color(_),
    onSurfaceVariant = Color(_),
    outline = Color(_),
    inverseOnSurface = Color(_),
    inverseSurface = Color(_),
    inversePrimary = Color(_),
    surfaceTint = Color(_),
    outlineVariant = Color(_),
    scrim = Color(_),
)
*/

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF5A4B63),        // Новый: нейтральный серо-фиолетовый
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE0D9EC), // Согласован с новым primary
    onPrimaryContainer = Color(0xFF2B1C35), // Тёмный текст на контейнере
    secondary = Color(0xFF4B635A),       // Без изменений — как тебе нравится
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCFE7DD),
    onSecondaryContainer = Color(0xFF081F17),
    tertiary = Color(0xFF4B635A),        // ← Опционально: можно сделать tertiary = secondary, если не используется
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFCFE7DD),
    onTertiaryContainer = Color(0xFF081F17),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFDEE4E8),
    onSurfaceVariant = Color(0xFF42484D),
    outline = Color(0xFF72797E),
    inverseOnSurface = Color(0xFFF0F0F2),
    inverseSurface = Color(0xFF2E3133),
    inversePrimary = Color(0xFFC9BCE3),  // Светлый primary для тёмной темы
    surfaceTint = Color(0xFF5A4B63),
    outlineVariant = Color(0xFFC2C8CC),
    scrim = Color(0xFF000000),
)

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFC9BCE3),        // Светлый оттенок для тёмной темы
    onPrimary = Color(0xFF2B1C35),      // Тёмный текст на primary
    primaryContainer = Color(0xFF41324A), // Тёмный контейнер
    onPrimaryContainer = Color(0xFFE0D9EC), // Светлый текст на контейнере
    secondary = Color(0xFFB3CCBF),      // Без изменений
    onSecondary = Color(0xFF1D352D),
    secondaryContainer = Color(0xFF334B43),
    onSecondaryContainer = Color(0xFFCFE7DD),
    tertiary = Color(0xFFB3CCBF),
    onTertiary = Color(0xFF1D352D),
    tertiaryContainer = Color(0xFF334B43),
    onTertiaryContainer = Color(0xFFCFE7DD),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF42484D),
    onSurfaceVariant = Color(0xFFC2C8CC),
    outline = Color(0xFF8C9297),
    inverseOnSurface = Color(0xFF1A1C1E),
    inverseSurface = Color(0xFFE2E2E6),
    inversePrimary = Color(0xFF5A4B63),  // Тёмный primary для светлой темы (inverse)
    surfaceTint = Color(0xFFC9BCE3),
    outlineVariant = Color(0xFF42484D),
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
