package ru.despectra.proxiya.controller.repository

import ru.despectra.proxiya.controller.utils.makeGenericErrorAsException
import ru.despectra.proxiya.proto.errors.ErrorType
import ru.despectra.proxiya.proto.proxy.ProxyParams
import ru.despectra.proxiya.proto.proxy.ProxyType
import ru.despectra.proxiya.proto.proxy.reverseProxyParamsOrNull
import ru.despectra.proxiya.proto.proxy.transparentProxyParamsOrNull
import ru.despectra.proxiya.proto.proxy.upstreamProxyParamsOrNull

class ProxyParamsValidator {
    fun validate(params: ProxyParams): Result<Boolean> = runCatching {
        if (params.name.trim().isEmpty()) {
            throw makeGenericErrorAsException(
                ErrorType.PROXY_PARAMS_INCOMPLETE,
                "Proxy name is required"
            )
        }

        if (params.port !in 0..65535) {
            throw makeGenericErrorAsException(
                ErrorType.PROXY_PARAMS_INVALID,
                "Proxy port ${params.port} is incorrect"
            )
        }

        if (params.type == ProxyType.PROXY_TYPE_UPSTREAM && params.upstreamProxyParamsOrNull == null) {
            throw makeGenericErrorAsException(
                ErrorType.PROXY_PARAMS_INCOMPLETE,
                "Upstream proxy params are required"
            )
        }

        if (params.type == ProxyType.PROXY_TYPE_REVERSE && params.reverseProxyParamsOrNull == null) {
            throw makeGenericErrorAsException(
                ErrorType.PROXY_PARAMS_INCOMPLETE,
                "Reverse proxy params are required"
            )
        }

        if (params.type == ProxyType.PROXY_TYPE_TRANSPARENT && params.transparentProxyParamsOrNull == null) {
            throw makeGenericErrorAsException(
                ErrorType.PROXY_PARAMS_INCOMPLETE,
                "Transparent proxy params are required"
            )
        }

        true
    }
}