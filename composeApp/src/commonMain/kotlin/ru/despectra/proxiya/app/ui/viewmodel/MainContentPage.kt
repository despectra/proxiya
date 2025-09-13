package ru.despectra.proxiya.app.ui.viewmodel

sealed class MainContentPage {
    data object CreateProxyPage : MainContentPage()
    data class SelectedProxyPage(val proxyId: String) : MainContentPage()
    data object SettingsPage : MainContentPage()
}