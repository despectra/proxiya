package ru.despectra.proxiya.controller

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import ru.despectra.proxiya.controller.di.initProxiyaDependencyInjection
import ru.despectra.proxiya.controller.server.ProxiyaControllerServer
import kotlin.system.exitProcess

object ProxiyaControllerApp {

    private val logger = LoggerFactory.getLogger(ProxiyaControllerApp::class.java)

    fun launch(args: Array<String>) {
        logger.info("Starting Proxiya Controller App...")

        runBlocking {
            try {
                val config = AppStartConfig.fromArgs(args)
                val dependencyInjectionContainer = initProxiyaDependencyInjection()
                val server = dependencyInjectionContainer.koin.get<ProxiyaControllerServer>()
                server.start(config)

                logger.info("Proxiya Controller App is running. Press Ctrl+C to stop.")

                // Обрабатываем сигналы завершения
                Runtime.getRuntime().addShutdownHook(Thread {
                    logger.info("Shutdown signal received, stopping server...")
                    dependencyInjectionContainer.close()
                    server.stop()
                })

                // Ждем бесконечно (пока не получим сигнал завершения)
                while (isActive) {
                    delay(1000)
                }

            } catch (e: Exception) {
                logger.error("Fatal error in Proxiya Controller App", e)
                exitProcess(1)
            }
        }
    }
}