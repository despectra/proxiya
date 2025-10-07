package ru.despectra.proxiya.app.ui.vo

data class ProxyItemVo(
    val id: String,
    val name: String,
    val type: String,
    val port: String,
    val status: Status,
) {

    enum class Status {
        IDLE,
        TRANSIENT, // stopping or starting
        ACTIVE,
        ERROR
    }
}
