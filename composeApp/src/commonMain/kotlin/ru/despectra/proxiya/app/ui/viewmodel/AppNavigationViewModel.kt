package ru.despectra.proxiya.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.logging.Logger

class AppNavigationViewModel : ViewModel() {
    private val _currentPage = MutableStateFlow<MainContentPage>(MainContentPage.CreateProxyPage)
    val currentPage = _currentPage.asStateFlow()

    fun selectPage(page: MainContentPage) {
        _currentPage.value = page
        Logger.getLogger(this.javaClass.name).info("Select page: $page")
    }
}