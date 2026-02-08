package ru.despectra.proxiya.app.ui.vo

import ru.despectra.proxiya.app.data.proxy.ProxyType

data class CreateProxyFormVo(
    val name: String,
    val proxyType: ProxyType,
    val port: String,
    val additionalParams: String,
    val reverseProxyTargets: String,
) {

    companion object {
        val init = CreateProxyFormVo(
            name = "",
            proxyType = ProxyType.Https,
            port = "",
            additionalParams = "",
            reverseProxyTargets = "",
        )
    }

    sealed interface SubmitResult {
        data object Init : SubmitResult
        data object InProgress : SubmitResult
        data object Success : SubmitResult
        data class Error(val message: String) : SubmitResult
    }
}