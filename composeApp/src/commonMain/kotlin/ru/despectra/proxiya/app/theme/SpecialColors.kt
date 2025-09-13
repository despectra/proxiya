package ru.despectra.proxiya.app.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Data class для хранения кастомных цветов
 */
data class SpecialColorScheme(
    val success: Color,
    val warning: Color,
    val info: Color,

    // Proxy statuses
    val proxyStatusInactive: Color,
    val proxyStatusRunning: Color,
    val proxyStatusError: Color,
)

/**
 * Кастомные цвета для светлой темы
 */
val LightSpecialColorScheme = SpecialColorScheme(
    success = Color(0xFF22AA11),
    warning = Color(0xFFFF8C00),
    info = Color(0xFF2196F3),

    proxyStatusInactive = Color(0xFF4B4B4B),
    proxyStatusRunning = Color(0xFF064006),
    proxyStatusError = Color(0xFFFF0000),
)

/**
 * Кастомные цвета для темной темы
 */
val DarkSpecialColorScheme = SpecialColorScheme(
    success = Color(0xFF4CAF50),
    warning = Color(0xFFFFB74D),
    info = Color(0xFF64B5F6),

    proxyStatusInactive = Color(0xFF555555),
    proxyStatusRunning = Color(0xFF007200),
    proxyStatusError = Color(0xFFFF0000),
)

/**
 * CompositionLocal для доступа к кастомным цветам в любом месте UI
 */
val LocalSpecialColors = compositionLocalOf { LightSpecialColorScheme }

/**
 * Composable для доступа к кастомным цветам
 */
val SpecialColors: SpecialColorScheme
    @Composable @ReadOnlyComposable get() = LocalSpecialColors.current