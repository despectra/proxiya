package ru.despectra.proxiya.app.service

import io.grpc.ManagedChannel
import io.grpc.kotlin.AbstractCoroutineStub
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

abstract class GrpcServiceWrapper<S : AbstractCoroutineStub<S>> : Closeable {

    private var channel: ManagedChannel? = null
    private var serviceCoroutineStub: S? = null

    abstract fun createChannel(): ManagedChannel

    abstract fun createServiceStub(channel: ManagedChannel): S

    fun initialize() {
        val channel = createChannel()
        this.channel = channel
        this.serviceCoroutineStub = createServiceStub(channel)
    }

    protected fun getService(): S {
        if (serviceCoroutineStub == null) {
            connectionError()
        }
        return serviceCoroutineStub!!
    }

    protected suspend fun <T> callService(
        coroutineContext: CoroutineContext = Dispatchers.IO,
        callFn: suspend S.() -> T
    ): T {
        val service = getService()
        return withContext(coroutineContext) {
            service.callFn()
        }
    }

    private fun connectionError(message: String = ""): Nothing {
        throw IllegalStateException("gRPC client connection error $message".trim())
    }

    override fun close() {
        channel?.shutdownNow()
        channel = null
        serviceCoroutineStub = null
    }
}