package ru.despectra.proxiya.controller.config.proxy

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class ProxyConfig(
    val name: String,
    val type: String,
    val port: Int,
    val upstreamProxyParams: UpstreamProxyParamsConfig? = null,
    val reverseProxyParams: ReverseProxyParamsConfig? = null,
    val hostsWhitelist: List<String>,
    val hostsBlacklist: List<String>,
    val extraCommandParams: List<String>,
    val autostart: Boolean,
    val createdAt: @Contextual LocalDateTime,
)
