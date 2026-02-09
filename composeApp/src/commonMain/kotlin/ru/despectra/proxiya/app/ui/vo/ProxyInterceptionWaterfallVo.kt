package ru.despectra.proxiya.app.ui.vo

data class ProxyInterceptionWaterfallVo(
    val interceptedRequests: List<InterceptedRequestVo>,
)

data class InterceptedRequestVo(
    val id: String,
    val timestamp: Long,
    val date: String,
    val time: String,
    val httpMethod: String,
    val host: String,
    val urlPath: String,
    val status: RequestStatusVo,
    val measuredParams: RequestMeasuredParams,
)

sealed interface RequestStatusVo {
    data object Ongoing : RequestStatusVo
    data class Completed(
        val httpStatusCode: String,
        val kind: StatusKind,
    ) : RequestStatusVo
    data class Failed(
        val errorMessage: String
    ) : RequestStatusVo

    enum class StatusKind {
        // 2xx or 3xx
        Success,

        // 4xx
        ClientError,

        // 5xx
        ServerError
    }
}

data class RequestMeasuredParams(
    val timing: String,
    val requestSize: String,
    val responseSize: String,
)
