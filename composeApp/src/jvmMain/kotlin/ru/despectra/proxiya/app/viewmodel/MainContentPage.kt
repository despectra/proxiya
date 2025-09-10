package ru.despectra.proxiya.app.viewmodel

sealed class MainContentPage {
    data object CreateProxyPage : MainContentPage()
    data class SelectedProxyPage(val proxyId: String) : MainContentPage()
    data object SettingsPage : MainContentPage()
}