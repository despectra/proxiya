package ru.despectra.proxiya.controller.interception

import ru.despectra.proxiya.proto.data.interception.InterceptedRequestNotification
import ru.despectra.proxiya.proto.data.interception.InterceptedResponseNotification
import ru.despectra.proxiya.proto.data.interception.RequestProcessingResult
import ru.despectra.proxiya.proto.data.interception.ResponseProcessingResult

interface InterceptedEventsConsumer {
    suspend fun acceptRequest(request: InterceptedRequestNotification): RequestProcessingResult
    suspend fun acceptResponse(response: InterceptedResponseNotification): ResponseProcessingResult
}