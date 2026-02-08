package ru.despectra.proxiya.app.data.proxy

import kotlinx.coroutines.flow.Flow
import ru.despectra.proxiya.app.service.ProxyControlService
import ru.despectra.proxiya.proto.common.Endpoint
import ru.despectra.proxiya.proto.common.endpoint
import ru.despectra.proxiya.proto.proxy.proxyParams
import ru.despectra.proxiya.proto.proxy.reverseProxyParams
import ru.despectra.proxiya.proto.service.control.ProxiesList
import java.util.logging.Level
import java.util.logging.Logger
import ru.despectra.proxiya.proto.proxy.ProxyType as ProtoProxyType

class ProxiesRepository(
    private val proxyControlService: ProxyControlService,
) {

    private val logger = Logger.getLogger(ProxiesRepository::class.java.name)

    private val uriRegexp = Regex("([a-z0-9]+)://([a-zA-Z0-9.]+):?([0-9]*)")

    suspend fun createProxy(
        name: String,
        proxyType: ProxyType,
        port: Int,
        additionalParams: String,
        reverseProxyTargets: String
    ): Result<String> {
        val proxyParams = proxyParams {
            this.name = name
            this.type = when (proxyType) {
                ProxyType.Https -> ProtoProxyType.PROXY_TYPE_HTTP
                ProxyType.Socks5 -> ProtoProxyType.PROXY_TYPE_SOCKS5
                ProxyType.Reverse -> ProtoProxyType.PROXY_TYPE_REVERSE
            }
            this.port = port
            this.extraCmdParams.addAll(additionalParams.split(" "))
            if (proxyType == ProxyType.Reverse && reverseProxyTargets.isNotBlank()) {
                this.reverseProxyParams = reverseProxyParams {
                    reverseProxyTargets.split(" ")
                        .mapNotNull(::mapEndpoint)
                        .let { this.destinations.addAll(it) }
                }
            }
        }
        return runCatching {
            proxyControlService.createProxy(proxyParams).proxyId
        }.onFailure { e ->
            logger.log(Level.SEVERE, "Failed to create proxy", e)
        }
    }

    suspend fun deleteProxy(proxyId: String) {
        try {
            proxyControlService.deleteProxy(proxyId)
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Failed to delete proxy", e)
        }
    }

    suspend fun startProxy(proxyId: String) {
        try {
            proxyControlService.startProxy(proxyId)
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Failed to start proxy", e)
        }
    }

    suspend fun stopProxy(proxyId: String) {
        try {
            proxyControlService.stopProxy(proxyId)
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Failed to stop proxy", e)
        }
    }

    suspend fun toggleProxyInterception(proxyId: String, isEnabled: Boolean) {
        try {
            proxyControlService.toggleProxyInterception(proxyId, isEnabled)
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Failed to toggle proxy interception", e)
        }
    }

    fun proxiesFlow(): Flow<ProxiesList> {
        return proxyControlService.proxiesListStream
    }

    private fun mapEndpoint(target: String): Endpoint? {
        if (!target.matches(uriRegexp)) {
            return null
        }
        return uriRegexp.matchEntire(target)?.groups?.let {
            val scheme = it[1]?.value
            val host = it[2]?.value
            val port = it[3]?.value
            endpoint {
                scheme?.let {
                    this.scheme = scheme
                }
                host?.let {
                    this.host = host
                }
                port?.let {
                    this.port = port.toInt()
                }
            }
        }
    }
}