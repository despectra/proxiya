package ru.despectra.proxiya.app.ui.vo

import ru.despectra.proxiya.proto.proxy.ProcessState
import ru.despectra.proxiya.proto.proxy.ProxyType
import ru.despectra.proxiya.proto.service.control.ProxiesList

fun ProxiesList.toViewObject(): List<ProxyItemVo> {
    return this.proxyInstanceList.map { proxyInfo ->
        val params = proxyInfo.params
        ProxyItemVo(
            id = proxyInfo.proxyId,
            name = params.name,
            type = when (params.type) {
                ProxyType.PROXY_TYPE_HTTP -> "HTTPS"
                ProxyType.PROXY_TYPE_SOCKS5 -> "SOCKS5"
                // TODO other types
                else -> "Unsupported type"
            },
            port = params.port.toString(),
            status = when (proxyInfo.processStatus.state) {
                ProcessState.PROCESS_STATE_STOPPED -> ProxyItemVo.Status.IDLE

                ProcessState.PROCESS_STATE_STARTING,
                ProcessState.PROCESS_STATE_STOPPING -> ProxyItemVo.Status.TRANSIENT

                ProcessState.PROCESS_STATE_RUNNING -> ProxyItemVo.Status.ACTIVE

                // TODO other types + ProxyState
                else -> ProxyItemVo.Status.ERROR
            }
        )
    }
}