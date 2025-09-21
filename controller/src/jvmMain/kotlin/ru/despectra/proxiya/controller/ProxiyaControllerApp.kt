package ru.despectra.proxiya.controller

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import ru.despectra.proxiya.controller.controller.ControllerConfig
import ru.despectra.proxiya.controller.controller.ProxiyaController
import kotlin.system.exitProcess

object ProxiyaControllerApp {

    private val logger = LoggerFactory.getLogger(ProxiyaControllerApp::class.java)

    fun launch(args: Array<String>) {
        logger.info("Starting Proxiya Controller...")

        val config = parseArguments(args)
        val controller = ProxiyaController(
            port = config.port,
            host = config.host
        )

        runBlocking {
            try {
                controller.start()
                logger.info("Proxiya Controller is running. Press Ctrl+C to stop.")

                // Обрабатываем сигналы завершения
                Runtime.getRuntime().addShutdownHook(Thread {
                    logger.info("Shutdown signal received, stopping controller...")
                    runBlocking {
                        controller.stop()
                    }
                })

                // Ждем бесконечно (пока не получим сигнал завершения)
                while (isActive) {
                    delay(1000)
                }

            } catch (e: Exception) {
                logger.error("Fatal error in Proxiya Controller", e)
                exitProcess(1)
            }
        }
    }

    /**
     * Парсинг аргументов командной строки
     */
    private fun parseArguments(args: Array<String>): ControllerConfig {
        var port = 50051
        var host = "0.0.0.0"

        var i = 0
        while (i < args.size) {
            when (args[i]) {
                "--port", "-p" -> {
                    if (i + 1 < args.size) {
                        port = args[i + 1].toIntOrNull() ?: run {
                            logger.error("Invalid port number: ${args[i + 1]}")
                            exitProcess(1)
                        }
                        i++
                    } else {
                        logger.error("Port number not specified")
                        exitProcess(1)
                    }
                }
                "--host", "-h" -> {
                    if (i + 1 < args.size) {
                        host = args[i + 1]
                        i++
                    } else {
                        logger.error("Host not specified")
                        exitProcess(1)
                    }
                }
                "--help" -> {
                    printUsage()
                    exitProcess(0)
                }
                else -> {
                    logger.error("Unknown argument: ${args[i]}")
                    printUsage()
                    exitProcess(1)
                }
            }
            i++
        }

        return ControllerConfig(port, host)
    }

    /**
     * Вывод справки по использованию
     */
    private fun printUsage() {
        println("""
            Proxiya Controller - HTTP Proxy Management System
            
            Usage: java -jar controller.jar [options]
            
            Options:
              -p, --port PORT     Port for gRPC server (default: 50051)
              -h, --host HOST     Host for gRPC server (default: 0.0.0.0)
              --help              Show this help message
            
            Environment Variables:
              PROXIYA_PORT        Port for gRPC server
              PROXIYA_HOST        Host for gRPC server
            
            Examples:
              java -jar controller.jar --port 8080 --host localhost
              java -jar controller.jar -p 50051 -h 0.0.0.0
        """.trimIndent())
    }
}