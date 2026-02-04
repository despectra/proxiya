package ru.despectra.proxiya.controller.interception

import kotlinx.coroutines.flow.Flow
import ru.despectra.proxiya.proto.data.interception.InterceptedRequestNotification
import ru.despectra.proxiya.proto.data.interception.InterceptedResponseNotification

interface InterceptedEventsProducer {
    fun requests(): Flow<InterceptedRequestNotification>
    fun responses(): Flow<InterceptedResponseNotification>
}