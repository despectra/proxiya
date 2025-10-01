package ru.despectra.proxiya.controller.config.proxies.v2

import kotlinx.serialization.Serializable

@Serializable
data class EndpointConfig(
    val host: String,
    val port: Int,
)
