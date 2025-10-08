package ru.despectra.proxiya.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Data class для хранения кастомных цветов
 */
data class CustomColorScheme(
    // General status colors
    val success: Color,
    val warning: Color,
    val info: Color,
    
    // Status colors
    val statusIdle: Color,
    val statusStarting: Color,
    val statusActive: Color,
    val statusStopping: Color,
    val statusError: Color,
    val statusStreaming: Color,
    
    // Proxy statuses (legacy)
    val proxyStatusInactive: Color,
    val proxyStatusRunning: Color,
    val proxyStatusError: Color,
)

/**
 * Кастомные цвета для светлой темы
 */
val LightCustomColorScheme = CustomColorScheme(
    // General status colors
    success = Color(0xFF22AA11),
    warning = Color(0xFFFF8C00),
    info = Color(0xFF2196F3),
    
    // Status colors from Color.kt
    statusIdle = Color(0xFF006874),
    statusStarting = Color(0xFF425E91),
    statusActive = Color(0xFF48672E),
    statusStopping = Color(0xFF006A66),
    statusError = Color(0xFF904A41),
    statusStreaming = Color(0xFF2E6A44),
    
    // Proxy statuses (legacy)
    proxyStatusInactive = Color(0xFF006874),
    proxyStatusRunning = Color(0xFF48672E),
    proxyStatusError = Color(0xFF904A41),
)

/**
 * Кастомные цвета для темной темы
 */
val DarkCustomColorScheme = CustomColorScheme(
    // General status colors
    success = Color(0xFF4CAF50),
    warning = Color(0xFFFFB74D),
    info = Color(0xFF64B5F6),
    
    // Status colors from Color.kt
    statusIdle = Color(0xFF82D3E0),
    statusStarting = Color(0xFFABC7FF),
    statusActive = Color(0xFFAED18D),
    statusStopping = Color(0xFF80D5D0),
    statusError = Color(0xFFFFB4A9),
    statusStreaming = Color(0xFF96D5A6),
    
    // Proxy statuses (legacy)
    proxyStatusInactive = Color(0xFF82D3E0),
    proxyStatusRunning = Color(0xFFAED18D),
    proxyStatusError = Color(0xFFFFB4A9),
)

/**
 * CompositionLocal для доступа к кастомным цветам в любом месте UI
 */
val LocalCustomColors = compositionLocalOf { LightCustomColorScheme }

/**
 * Composable для доступа к кастомным цветам
 */
val ProxiyaCustomColors: CustomColorScheme
    @Composable @ReadOnlyComposable get() = LocalCustomColors.current