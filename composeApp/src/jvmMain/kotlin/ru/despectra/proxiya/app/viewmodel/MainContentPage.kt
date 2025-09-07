package ru.despectra.proxiya.app.viewmodel

sealed class MainContentPage {
    object CreateProxyPage : MainContentPage()
    data class SelectedProxyPage(val proxyId: String) : MainContentPage()
}