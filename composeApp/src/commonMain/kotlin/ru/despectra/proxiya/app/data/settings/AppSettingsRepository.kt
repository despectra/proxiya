package ru.despectra.proxiya.app.data.settings

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppSettingsRepository(
    private val settings: Settings
) {

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<AppSettings> = _settingsFlow.asStateFlow()
    
    private fun loadSettings(): AppSettings {
        return AppSettings(
            theme = ThemeMode.valueOf(
                settings.getString(KEY_THEME, AppSettings.DEFAULT_THEME)
            ),
            autoStartProxies = settings.getBoolean(KEY_AUTO_START_PROXIES, AppSettings.DEFAULT_AUTO_START)
        )
    }
    
    private fun saveSettings(appSettings: AppSettings) {
        settings.putString(KEY_THEME, appSettings.theme.name)
        settings.putBoolean(KEY_AUTO_START_PROXIES, appSettings.autoStartProxies)
    }
    
    fun updateSettings(update: (AppSettings) -> AppSettings) {
        val newSettings = update(_settingsFlow.value)
        _settingsFlow.value = newSettings
        saveSettings(newSettings)
    }
    
    fun updateTheme(theme: ThemeMode) {
        updateSettings { it.copy(theme = theme) }
    }
    
    fun updateAutoStartProxies(autoStart: Boolean) {
        updateSettings { it.copy(autoStartProxies = autoStart) }
    }

    companion object Companion {
        const val KEY_THEME = "theme"
        const val KEY_AUTO_START_PROXIES = "autoStartProxies"
    }
}
