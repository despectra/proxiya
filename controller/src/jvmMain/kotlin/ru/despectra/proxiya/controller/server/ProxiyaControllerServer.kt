package ru.despectra.proxiya.controller.server

import io.grpc.BindableService
import io.grpc.Server
import io.grpc.ServerBuilder
import org.slf4j.LoggerFactory
import ru.despectra.proxiya.controller.AppStartConfig
import ru.despectra.proxiya.controller.grpc.ProxyControlServiceImpl
import ru.despectra.proxiya.controller.grpc.ProxyDataServiceImpl
import ru.despectra.proxiya.controller.grpc.instance.ProxyInstanceControlServiceImpl
import ru.despectra.proxiya.controller.grpc.instance.ProxyInstanceDataServiceImpl

/**
 * Основной сервер контроллера Proxiya
 */
class ProxiyaControllerServer(
    private val proxyControlService: ProxyControlServiceImpl,
    private val proxyDataService: ProxyDataServiceImpl,
    private val proxyInstanceControlService: ProxyInstanceControlServiceImpl,
    private val proxyInstanceDataService: ProxyInstanceDataServiceImpl,
) {
    private val logger = LoggerFactory.getLogger(ProxiyaControllerServer::class.java)

    private var server: Server? = null

    /**
     * Запуск контроллера
     */
    fun start(appStartConfig: AppStartConfig) {
        logger.info("Starting Proxiya Controller Server...")
        // Создаем gRPC сервер
        server = ServerBuilder.forPort(appStartConfig.port)
            .addService(proxyControlService as BindableService)
            .addService(proxyDataService as BindableService)
            .addService(proxyInstanceControlService as BindableService)
            .addService(proxyInstanceDataService as BindableService)
            .build()

        // Запускаем сервер
        server?.start()

        logger.info("Proxiya Controller Server started successfully on port ${appStartConfig.port}")
    }

    /**
     * Остановка контроллера
     */
    fun stop() {
        logger.info("Stopping Proxiya Controller...")

        // Останавливаем gRPC сервер
        server?.shutdownNow()
        server?.awaitTermination()

        logger.info("Proxiya Controller stopped")
    }
}