package ru.despectra.proxiya.controller.config.proxies.v2

import kotlinx.serialization.Serializable

@Serializable
data class UpstreamProxyParamsConfig(
    val destination: EndpointConfig,
)

@Serializable
data class ReverseProxyParamsConfig(
    val destinations: List<EndpointConfig>,
)
