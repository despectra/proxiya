package ru.despectra.proxiya.controller.config.proxy

import kotlinx.serialization.Serializable
import ru.despectra.proxiya.controller.config.common.EndpointConfig

@Serializable
data class UpstreamProxyParamsConfig(
    val destination: EndpointConfig,
)

@Serializable
data class ReverseProxyParamsConfig(
    val destinations: List<EndpointConfig>,
)
