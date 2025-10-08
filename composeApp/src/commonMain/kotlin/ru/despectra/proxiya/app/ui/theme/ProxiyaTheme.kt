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

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2D4A57),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFAFC7D0),
    onPrimaryContainer = Color(0xFF001F29),
    secondary = Color(0xFF4B635A),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCFE7DD),
    onSecondaryContainer = Color(0xFF081F17),
    tertiary = Color(0xFF6B5A75),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFEFD9F6),
    onTertiaryContainer = Color(0xFF251530),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF8F9FA),      // Мягкий фон окна (чуть темнее surface)
    onBackground = Color(0xFF1A1C1E),    // Тёмный текст на фоне
    surface = Color(0xFFFFFFFF),         // Белые карточки и таблицы
    onSurface = Color(0xFF1A1C1E),       // Тот же текст — для единообразия
    surfaceVariant = Color(0xFFDEE4E8),  // Нейтральный серый для разделителей
    onSurfaceVariant = Color(0xFF42484D),
    outline = Color(0xFF72797E),
    inverseOnSurface = Color(0xFFF0F0F2),
    inverseSurface = Color(0xFF2E3133),
    inversePrimary = Color(0xFFA7CCD5),
    surfaceTint = Color(0xFF2D4A57),
    outlineVariant = Color(0xFFC2C8CC),
    scrim = Color(0xFF000000),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA7CCD5),
    onPrimary = Color(0xFF003542),
    primaryContainer = Color(0xFF1E4D59),
    onPrimaryContainer = Color(0xFFD0E4EC),
    secondary = Color(0xFFB3CCBF),
    onSecondary = Color(0xFF1D352D),
    secondaryContainer = Color(0xFF334B43),
    onSecondaryContainer = Color(0xFFCFE7DD),
    tertiary = Color(0xFFD5BCE3),
    onTertiary = Color(0xFF3A2A45),
    tertiaryContainer = Color(0xFF51405B),
    onTertiaryContainer = Color(0xFFEFD9F6),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF121212),      // Глубокий фон окна
    onBackground = Color(0xFFE2E2E6),    // Светлый текст на фоне
    surface = Color(0xFF1A1C1E),         // Карточки чуть светлее фона
    onSurface = Color(0xFFE2E2E6),       // Тот же текст — для согласованности
    surfaceVariant = Color(0xFF42484D),  // Соответствует новой тёмной базе
    onSurfaceVariant = Color(0xFFC2C8CC),
    outline = Color(0xFF8C9297),
    inverseOnSurface = Color(0xFF1A1C1E),
    inverseSurface = Color(0xFFE2E2E6),
    inversePrimary = Color(0xFF2D4A57),
    surfaceTint = Color(0xFFA7CCD5),
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
