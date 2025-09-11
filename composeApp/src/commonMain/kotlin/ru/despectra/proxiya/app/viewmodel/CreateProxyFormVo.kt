package ru.despectra.proxiya.app.viewmodel

import ru.despectra.proxiya.app.data.proxy.ProxyType

data class CreateProxyFormVo(
    val name: String,
    val proxyType: ProxyType,
    val port: String,
    val additionalParams: String,
) {

    companion object {
        val init = CreateProxyFormVo(
            name = "",
            proxyType = ProxyType.Https,
            port = "",
            additionalParams = "",
        )
    }
}