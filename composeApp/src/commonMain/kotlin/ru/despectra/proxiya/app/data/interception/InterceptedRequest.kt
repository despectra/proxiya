package ru.despectra.proxiya.app.data.interception

import ru.despectra.proxiya.proto.data.interception.InterceptedRequestNotification
import ru.despectra.proxiya.proto.data.interception.InterceptedResponseNotification

data class InterceptedRequest(
    val requestId: RequestId,
    val originRequest: InterceptedRequestNotification,
    val associatedResponse: InterceptedResponseNotification? = null
)

data class InterceptedRequestKey(
    val requestId: RequestId,
    val hasResponse: Boolean
)