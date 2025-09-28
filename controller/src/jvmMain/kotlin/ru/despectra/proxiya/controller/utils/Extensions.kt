package ru.despectra.proxiya.controller.utils

import com.google.protobuf.timestamp
import ru.despectra.proxiya.controller.data.ProxiyaGenericError
import ru.despectra.proxiya.proto.errors.GenericError
import java.time.Instant

fun Instant.asProtoTimestamp() = timestamp {
    seconds = epochSecond
    nanos = nano
}

fun GenericError.asException() = ProxiyaGenericError(this)