package ru.despectra.proxiya.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import ru.despectra.proxiya.app.theme.ThemeMode

class ThemeViewModel : ViewModel() {
    var themeMode by mutableStateOf(ThemeMode.SYSTEM)
        private set

    fun setTheme(mode: ThemeMode) {
        themeMode = mode
    }
}
