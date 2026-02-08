package ru.despectra.proxiya.controller

import kotlin.system.exitProcess

data class AppStartConfig(
    val port: Int,
    val host: String,
    val proxyLauncherPath: String,
) {
    companion object {
        fun fromArgs(args: Array<String>): AppStartConfig = parseArguments(args)

        /**
         * Парсинг аргументов командной строки
         */
        private fun parseArguments(args: Array<String>): AppStartConfig {
            var port = 50051
            var host = "0.0.0.0"
            var proxyLauncherPath: String = ""

            val iterator = args.iterator()
            while (iterator.hasNext()) {
                when (val arg = iterator.next()) {
                    "--port", "-p" -> port = parsePortArgument(iterator)
                    "--host", "-h" -> host = parseHostArgument(iterator)
                    "--proxy-launcher-path" -> proxyLauncherPath = parseProxyLauncherPath(iterator)
                    "--help" -> {
                        printUsage()
                        exitProcess(0)
                    }

                    else -> {
                        println("Unknown argument: $arg")
                        printUsage()
                        exitProcess(1)
                    }
                }
            }

            if (proxyLauncherPath.isBlank()) {
                println("--proxy-launcher-path argument is required")
                exitProcess(1)
            }

            return AppStartConfig(port, host, proxyLauncherPath)
        }

        private fun parsePortArgument(iterator: Iterator<String>): Int {
            val portValue = requireNextArgument(iterator, "Port number not specified")
            return portValue.toIntOrNull() ?: run {
                println("Invalid port number: $portValue")
                exitProcess(1)
            }
        }

        private fun parseHostArgument(iterator: Iterator<String>): String {
            return requireNextArgument(iterator, "Host not specified")
        }

        private fun parseProxyLauncherPath(iterator: Iterator<String>): String {
            return requireNextArgument(iterator, "Proxy launcher script path not specified")
        }

        private fun requireNextArgument(iterator: Iterator<String>, errorMessage: String): String {
            if (!iterator.hasNext()) {
                println(errorMessage)
                exitProcess(1)
            }
            return iterator.next()
        }

        /**
         * Вывод справки по использованию
         */
        private fun printUsage() {
            println(
                """
                Proxiya Controller - HTTP Proxy Management System
                
                Usage: java -jar controller.jar [options]
                
                Options:
                  -p, --port PORT     Port for gRPC server (default: 50051)
                  -h, --host HOST     Host for gRPC server (default: 0.0.0.0)
                  --proxy-launcher-path       Path to script to start proxy
                  --help              Show this help message
                
                Environment Variables:
                  PROXIYA_PORT        Port for gRPC server
                  PROXIYA_HOST        Host for gRPC server
                
                Examples:
                  java -jar controller.jar --port 8080 --host localhost
                  java -jar controller.jar -p 50051 -h 0.0.0.0
            """.trimIndent()
            )
        }
    }
}