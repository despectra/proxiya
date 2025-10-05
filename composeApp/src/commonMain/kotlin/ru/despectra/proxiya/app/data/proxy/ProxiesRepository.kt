package ru.despectra.proxiya.app.data.proxy

import kotlinx.coroutines.flow.Flow
import ru.despectra.proxiya.app.service.ProxyControlService
import ru.despectra.proxiya.proto.proxy.proxyParams
import ru.despectra.proxiya.proto.service.control.ProxiesList
import java.util.logging.Level
import java.util.logging.Logger
import ru.despectra.proxiya.proto.proxy.ProxyType as ProtoProxyType

class ProxiesRepository(
    private val proxyControlService: ProxyControlService,
) {

    private val logger = Logger.getLogger(ProxiesRepository::class.java.name)

    suspend fun createProxy(name: String, proxyType: ProxyType, port: Int, additionalParams: String): Result<String> {
        val proxyParams = proxyParams {
            this.name = name
            this.type = when (proxyType) {
                ProxyType.Https -> ProtoProxyType.PROXY_TYPE_HTTP
                ProxyType.Socks5 -> ProtoProxyType.PROXY_TYPE_SOCKS5
            }
            this.port = port
            this.extraCmdParams.addAll(additionalParams.split(" "))
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

    fun proxiesFlow(): Flow<ProxiesList> {
        return proxyControlService.proxiesListStream
    }
}