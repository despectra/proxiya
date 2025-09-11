package ru.despectra.proxiya.app.data.proxy

import kotlinx.coroutines.flow.Flow
import ru.despectra.proxiya.app.service.ProxyControlService
import ru.despectra.proxiya.app.utils.generateProxyId
import ru.despectra.proxiya.proto.control.ProxyControl
import ru.despectra.proxiya.proto.control.ProxyManagementServiceOuterClass
import java.util.logging.Level
import java.util.logging.Logger

class ProxiesRepository(
    private val proxyControlService: ProxyControlService,
) {

    private val logger = Logger.getLogger(ProxiesRepository::class.java.name)

    suspend fun createProxy(name: String, proxyType: ProxyType, port: Int, additionalParams: String) {
        val proxyId = generateProxyId(
            name,
            mapOf(
                "port" to port.toString(),
                "type" to proxyType.name,
                "params" to additionalParams,
            )
        )
        val proxyConfig = ProxyControl.ProxyConfig.newBuilder()
            .setProxyId(proxyId)
            .setProxyName(name)
            .setPort(port)
            .setProxyType(when (proxyType) {
                ProxyType.Https -> ProxyControl.ProxyType.PROXY_HTTPS
                ProxyType.Socks5 -> ProxyControl.ProxyType.PROXY_SOCKS_5
            })
            .addMitmproxyArgs(additionalParams)
            .build()

        try {
            proxyControlService.createProxy(proxyConfig)
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Failed to create proxy", e)
            throw e
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

    suspend fun proxiesFlow(): Flow<ProxyManagementServiceOuterClass.ProxiesListState> {
        return proxyControlService.streamProxies()
    }
}