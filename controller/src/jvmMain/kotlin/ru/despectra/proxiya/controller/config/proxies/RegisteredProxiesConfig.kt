package ru.despectra.proxiya.controller.config.proxies

import kotlinx.serialization.Serializable

@Serializable
data class RegisteredProxiesConfig(
    val registeredProxies: Map<String /*Proxy ID*/, String /* Path to config */> = emptyMap()
) {
    companion object {
        fun default() = RegisteredProxiesConfig()
    }
}