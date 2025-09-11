package ru.despectra.proxiya.app.service

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ru.despectra.proxiya.proto.control.ProxyControl
import ru.despectra.proxiya.proto.control.ProxyManagementServiceGrpcKt
import ru.despectra.proxiya.proto.control.ProxyManagementServiceOuterClass.DeleteProxyRequest
import ru.despectra.proxiya.proto.control.ProxyManagementServiceOuterClass.ProxiesListState
import ru.despectra.proxiya.proto.control.ProxyManagementServiceOuterClass.StartProxyRequest
import ru.despectra.proxiya.proto.control.ProxyManagementServiceOuterClass.StopProxyRequest
import ru.despectra.proxiya.proto.control.ProxyManagementServiceOuterClass.StreamProxiesRequest
import java.lang.AutoCloseable

class ProxyControlService : AutoCloseable {

    private var channel: ManagedChannel? = null
    private var serviceCoroutineStub: ProxyManagementServiceGrpcKt.ProxyManagementServiceCoroutineStub? = null

    fun initialize(host: String, port: Int) {
        channel = ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext()
            .build()
        serviceCoroutineStub = ProxyManagementServiceGrpcKt.ProxyManagementServiceCoroutineStub(channel!!)
    }

    suspend fun createProxy(proxyConfig: ProxyControl.ProxyConfig): ProxyControl.ProxyInfo {
        return withContext(Dispatchers.IO) {
            serviceCoroutineStub?.createProxy(
                proxyConfig
            ) ?: connectionError()
        }
    }

    suspend fun deleteProxy(proxyId: String) {
        return withContext(Dispatchers.IO) {
            serviceCoroutineStub?.deleteProxy(
                request = DeleteProxyRequest.newBuilder().setProxyId(proxyId).build()
            ) ?: connectionError()
        }
    }

    suspend fun startProxy(proxyId: String) {
        return withContext(Dispatchers.IO) {
            serviceCoroutineStub?.startProxy(
                request = StartProxyRequest.newBuilder().setProxyId(proxyId).build()
            ) ?: connectionError()
        }
    }

    suspend fun stopProxy(proxyId: String) {
        return withContext(Dispatchers.IO) {
            serviceCoroutineStub?.stopProxy(
                request = StopProxyRequest.newBuilder().setProxyId(proxyId).build()
            ) ?: connectionError()
        }
    }

    suspend fun streamProxies(): Flow<ProxiesListState> = flow {
        serviceCoroutineStub?.streamProxies(
            request = StreamProxiesRequest.newBuilder().build()
        )?.collect { proxiesList ->
            emit(proxiesList)
        } ?: connectionError()
    }.flowOn(Dispatchers.IO)

    private fun connectionError(message: String = ""): Nothing {
        throw IllegalStateException("gRPC client connection error $message".trim())
    }

    override fun close() {
        channel?.shutdownNow()
        channel = null
        serviceCoroutineStub = null
    }
}