package ru.despectra.proxiya.app.service

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import ru.despectra.proxiya.proto.proxy.ProxyParams
import ru.despectra.proxiya.proto.service.control.CreateProxyResult
import ru.despectra.proxiya.proto.service.control.DeleteProxyResult
import ru.despectra.proxiya.proto.service.control.ProxiesList
import ru.despectra.proxiya.proto.service.control.ProxyControlServiceGrpcKt
import ru.despectra.proxiya.proto.service.control.UpdateProxyResult
import ru.despectra.proxiya.proto.service.control.createProxyRequest
import ru.despectra.proxiya.proto.service.control.deleteProxyRequest
import ru.despectra.proxiya.proto.service.control.proxiesList
import ru.despectra.proxiya.proto.service.control.startProxyRequest
import ru.despectra.proxiya.proto.service.control.stopProxyRequest
import ru.despectra.proxiya.proto.service.control.streamProxiesRequest
import ru.despectra.proxiya.proto.service.control.toggleProxyInterceptionRequest
import ru.despectra.proxiya.proto.service.control.updateProxyRequest

class ProxyControlService(
    val config: Config,
) : GrpcServiceWrapper<ProxyControlServiceGrpcKt.ProxyControlServiceCoroutineStub>() {

    data class Config(
        val host: String,
        val port: Int
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val proxiesListStream: Flow<ProxiesList> by lazy {
        getService()
            .streamProxies(streamProxiesRequest {  })
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = proxiesList {  }
            )
    }

    override fun createChannel(): ManagedChannel {
        return ManagedChannelBuilder.forAddress(config.host, config.port)
            .usePlaintext()
            .build()
    }

    override fun createServiceStub(
        channel: ManagedChannel
    ): ProxyControlServiceGrpcKt.ProxyControlServiceCoroutineStub {
        return ProxyControlServiceGrpcKt.ProxyControlServiceCoroutineStub(channel)
    }

    suspend fun createProxy(proxyParams: ProxyParams): CreateProxyResult {
        return callService {
            createProxy(
                createProxyRequest {
                    this.params = proxyParams
                }
            )
        }
    }

    suspend fun updateProxy(proxyId: String, proxyParams: ProxyParams, restart: Boolean): UpdateProxyResult {
        return callService {
            updateProxy(
                updateProxyRequest {
                    this.proxyId = proxyId
                    this.params = proxyParams
                    this.restartAfterUpdate = restart
                }
            )
        }
    }

    suspend fun deleteProxy(proxyId: String): DeleteProxyResult {
        return callService {
            deleteProxy(
                deleteProxyRequest {
                    this.proxyId = proxyId
                }
            )
        }
    }

    suspend fun startProxy(proxyId: String) {
        return callService {
            startProxy(
                startProxyRequest {
                    this.proxyId = proxyId
                }
            )
        }
    }

    suspend fun stopProxy(proxyId: String) {
        return callService {
            stopProxy(
                stopProxyRequest {
                    this.proxyId = proxyId
                }
            )
        }
    }

    suspend fun toggleProxyInterception(proxyId: String, isEnabled: Boolean) {
        return callService {
            toggleProxyInterception(
                toggleProxyInterceptionRequest {
                    this.proxyId = proxyId
                    this.interceptionEnabled = isEnabled
                }
            )
        }
    }

    override fun close() {
        super.close()
        scope.cancel()
    }
}