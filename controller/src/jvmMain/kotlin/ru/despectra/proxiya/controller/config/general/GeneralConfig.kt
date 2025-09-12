package ru.despectra.proxiya.controller.config.general

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeneralConfig(
    /**
     * Автоматически запускать прокси при старте контроллера
     */
    @SerialName("auto_start_proxies")
    val autoStartProxies: Boolean = false
) {
    companion object {
        fun default(): GeneralConfig = GeneralConfig()
    }
}
