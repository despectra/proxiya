package ru.despectra.proxiya.app.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import ru.despectra.proxiya.app.data.settings.AppSettings
import ru.despectra.proxiya.app.data.settings.AppSettingsRepository
import ru.despectra.proxiya.app.data.settings.ThemeMode

class SettingsViewModel(
    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {
    
    val settings: StateFlow<AppSettings> = appSettingsRepository.settingsFlow
    
    fun updateTheme(theme: ThemeMode) {
        appSettingsRepository.updateTheme(theme)
    }
    
    fun updateAutoStartProxies(autoStart: Boolean) {
        appSettingsRepository.updateAutoStartProxies(autoStart)
    }
}
