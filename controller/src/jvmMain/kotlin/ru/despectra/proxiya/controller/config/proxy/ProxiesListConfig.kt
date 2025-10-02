package ru.despectra.proxiya.controller.config.proxy

import kotlinx.serialization.Serializable
import ru.despectra.proxiya.controller.repository.ProxyId

@Serializable
data class ProxiesListConfig(
    val proxies: Map<ProxyId, ProxyConfig>
)
