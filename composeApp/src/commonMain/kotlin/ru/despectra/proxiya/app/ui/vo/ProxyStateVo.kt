package ru.despectra.proxiya.app.ui.vo

sealed class ProxyStateVo {
    object Loading : ProxyStateVo()
    data class Loaded(
        val name: String,
        val type: String,
        val port: String,
        val status: ProxyItemVo.Status,
        val processId: String?
    ) : ProxyStateVo()
    data class Error(val message: String) : ProxyStateVo()
}