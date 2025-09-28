package ru.despectra.proxiya.controller.data

import ru.despectra.proxiya.proto.errors.GenericError

class ProxiyaGenericError(
    val genericError: GenericError,
    override val message: String? = null
) : Exception(message)