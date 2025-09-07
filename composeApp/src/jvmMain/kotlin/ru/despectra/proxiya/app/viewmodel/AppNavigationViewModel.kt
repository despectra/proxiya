package ru.despectra.proxiya.app.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppNavigationViewModel : ViewModel() {
    private val _currentPage = MutableStateFlow<MainContentPage>(MainContentPage.CreateProxyPage)
    val currentPage = _currentPage.asStateFlow()

    fun selectPage(page: MainContentPage) {
        _currentPage.value = page
    }
}