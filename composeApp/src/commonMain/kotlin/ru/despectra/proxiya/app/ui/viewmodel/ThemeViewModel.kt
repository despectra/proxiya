package ru.despectra.proxiya.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.despectra.proxiya.app.data.settings.AppSettingsRepository
import ru.despectra.proxiya.app.data.settings.ThemeMode

class ThemeViewModel(
    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {

    val appTheme: StateFlow<ThemeMode>
        get() = appSettingsRepository.settingsFlow
            .map { it.theme }
            .stateIn(viewModelScope, SharingStarted.Lazily, ThemeMode.SYSTEM)
}
