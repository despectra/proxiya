package ru.despectra.proxiya.controller.config.proxies

import kotlinx.serialization.Serializable

@Serializable
data class ProxyConfig(
    val id: String,
    val name: String,
    val type: String,
    val port: Int,
    val additionalParams: String?,
    val createdAt: Long,
)