package ru.despectra.proxiya.controller.config.common

import kotlinx.serialization.Serializable

@Serializable
data class EndpointConfig(
    val host: String,
    val port: Int,
)