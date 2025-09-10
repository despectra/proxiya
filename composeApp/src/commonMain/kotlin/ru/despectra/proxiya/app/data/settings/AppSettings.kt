package ru.despectra.proxiya.app.data.settings

/**
 * Модель настроек приложения
 */
data class AppSettings(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val autoStartProxies: Boolean = false
) {
    companion object {
        val DEFAULT_THEME = ThemeMode.SYSTEM.name
        const val DEFAULT_AUTO_START = false
    }
}
