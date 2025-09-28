package ru.despectra.proxiya.controller.utils

import ru.despectra.proxiya.controller.data.ProxiyaGenericError
import ru.despectra.proxiya.proto.errors.ErrorType
import ru.despectra.proxiya.proto.errors.GenericError
import ru.despectra.proxiya.proto.errors.genericError

fun makeGenericError(type: ErrorType, message: String = ""): GenericError {
    return genericError {
        this.type = type
        this.errorMessage = message
    }
}

fun makeGenericErrorAsException(type: ErrorType, message: String = ""): Exception {
    return ProxiyaGenericError(makeGenericError(type, message))
}