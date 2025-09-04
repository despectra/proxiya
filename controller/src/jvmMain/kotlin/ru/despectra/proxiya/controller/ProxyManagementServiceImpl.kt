package ru.despectra.proxiya.controller

import org.slf4j.LoggerFactory
import ru.despectra.proxiya.proto.common.CommonTypes
import ru.despectra.proxiya.proto.control.ProxyControl
import ru.despectra.proxiya.proto.control.ProxyManagementServiceGrpcKt
import ru.despectra.proxiya.proto.control.ProxyManagementServiceOuterClass
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Реализация gRPC сервиса для управления прокси
 */
class ProxyManagementServiceImpl(
    private val controller: ProxiyaController
) : ProxyManagementServiceGrpcKt.ProxyManagementServiceCoroutineImplBase() {
    
    private val logger = LoggerFactory.getLogger(ProxyManagementServiceImpl::class.java)
    
    override suspend fun createProxy(request: ProxyControl.ProxyConfig): ProxyControl.ProxyInfo {
        try {
            logger.info("Creating proxy with ID: ${request.proxyId}")
            
            // Проверяем, не существует ли уже прокси с таким ID
            if (controller.getProxy(request.proxyId) != null) {
                throw IllegalArgumentException("Proxy with ID '${request.proxyId}' already exists")
            }
            
            // Создаем информацию о прокси
            val proxyInfo = ProxyControl.ProxyInfo.newBuilder()
                .setProxyId(request.proxyId)
                .setPort(request.port)
                .setStatus(ProxyControl.ProxyStatus.PROXY_STATUS_STOPPED)
                .setCreatedAt(System.currentTimeMillis())
                .setLastUpdated(System.currentTimeMillis())
                .build()
            
            // Добавляем прокси в реестр
            controller.addProxy(proxyInfo)
            
            logger.info("Proxy ${request.proxyId} created successfully")
            
            return proxyInfo
            
        } catch (e: Exception) {
            logger.error("Error creating proxy ${request.proxyId}", e)
            throw e
        }
    }
    
    override suspend fun startProxy(request: ProxyManagementServiceOuterClass.StartProxyRequest): ProxyControl.ProxyInfo {
        try {
            logger.info("Starting proxy: ${request.proxyId}")

            val proxyInfo = controller.getProxy(request.proxyId)
                ?: throw IllegalArgumentException("Proxy '${request.proxyId}' not found")

            if (proxyInfo.status == ProxyControl.ProxyStatus.PROXY_STATUS_RUNNING) {
                throw IllegalStateException("Proxy '${request.proxyId}' is already running")
            }
            
            // Обновляем статус на "запускается"
            controller.updateProxy(request.proxyId) { current ->
                current.toBuilder().setStatus(ProxyControl.ProxyStatus.PROXY_STATUS_STARTING).build()
            }
            
            // Запускаем процесс mitmproxy
            val process = startMitmproxyProcess(proxyInfo)
            
            if (process != null) {
                // Добавляем процесс в реестр
                controller.addProxyProcess(request.proxyId, process)
                
                logger.info("Proxy ${request.proxyId} started successfully (PID: ${process.pid()})")
                
                // Возвращаем обновленную информацию о прокси
                return controller.getProxy(request.proxyId)!!
            } else {
                // Обновляем статус на "ошибка"
                controller.updateProxy(request.proxyId) { current ->
                    current.toBuilder()
                        .setStatus(ProxyControl.ProxyStatus.PROXY_STATUS_ERROR)
                        .setErrorMessage("Failed to start mitmproxy process")
                        .build()
                }
                
                throw RuntimeException("Failed to start mitmproxy process")
            }
            
        } catch (e: Exception) {
            logger.error("Error starting proxy ${request.proxyId}", e)
            
            // Обновляем статус на "ошибка"
            controller.updateProxy(request.proxyId) { current ->
                current.toBuilder()
                    .setStatus(ProxyControl.ProxyStatus.PROXY_STATUS_ERROR)
                    .setErrorMessage("Startup error: ${e.message}")
                    .build()
            }
            
            throw e
        }
    }
    
    override suspend fun stopProxy(request: ProxyManagementServiceOuterClass.StopProxyRequest): ProxyControl.ProxyInfo {
        try {
            logger.info("Stopping proxy: ${request.proxyId}")
            
            val proxyInfo = controller.getProxy(request.proxyId)
            if (proxyInfo == null) {
                throw IllegalArgumentException("Proxy '${request.proxyId}' not found")
            }
            
            if (proxyInfo.status == ProxyControl.ProxyStatus.PROXY_STATUS_STOPPED) {
                throw IllegalStateException("Proxy '${request.proxyId}' is already stopped")
            }
            
            // Обновляем статус на "останавливается"
            controller.updateProxy(request.proxyId) { current ->
                current.toBuilder().setStatus(ProxyControl.ProxyStatus.PROXY_STATUS_STOPPING).build()
            }
            
            // Останавливаем процесс
            val success = stopMitmproxyProcess(request.proxyId)
            
            if (success) {
                // Обновляем статус на "остановлен"
                controller.updateProxy(request.proxyId) { current ->
                    current.toBuilder()
                        .setStatus(ProxyControl.ProxyStatus.PROXY_STATUS_STOPPED)
                        .setLastUpdated(System.currentTimeMillis())
                        .build()
                }
                
                logger.info("Proxy ${request.proxyId} stopped successfully")
                
                // Возвращаем обновленную информацию о прокси
                return controller.getProxy(request.proxyId)!!
            } else {
                // Обновляем статус на "ошибка"
                controller.updateProxy(request.proxyId) { current ->
                    current.toBuilder()
                        .setStatus(ProxyControl.ProxyStatus.PROXY_STATUS_ERROR)
                        .setErrorMessage("Failed to stop mitmproxy process")
                        .build()

                }
                
                throw RuntimeException("Failed to stop mitmproxy process")
            }
            
        } catch (e: Exception) {
            logger.error("Error stopping proxy ${request.proxyId}", e)
            
            // Обновляем статус на "ошибка"
            controller.updateProxy(request.proxyId) { current ->
                current.toBuilder()
                    .setStatus(ProxyControl.ProxyStatus.PROXY_STATUS_ERROR)
                    .setErrorMessage("Stop error: ${e.message}")
                    .build()
            }
            
            throw e
        }
    }
    
    override suspend fun getProxy(request: ProxyManagementServiceOuterClass.GetProxyRequest): ProxyControl.ProxyInfo {
        try {
            val proxyInfo = controller.getProxy(request.proxyId)
            
            if (proxyInfo == null) {
                throw IllegalArgumentException("Proxy '${request.proxyId}' not found")
            }
            
            return proxyInfo
            
        } catch (e: Exception) {
            logger.error("Error getting proxy ${request.proxyId}", e)
            throw e
        }
    }
    
    override suspend fun listProxies(request: ProxyManagementServiceOuterClass.ListProxiesRequest): ProxyManagementServiceOuterClass.ListProxiesResponse {
        try {
            val allProxies = controller.getProxies()
            
            // Фильтруем по статусу, если указан фильтр
            val filteredProxies = if (request.hasStatusFilter()) {
                allProxies.values.filter { it.status == request.statusFilter }
            } else {
                allProxies.values
            }
            
            return ProxyManagementServiceOuterClass.ListProxiesResponse.newBuilder()
                .addAllProxies(filteredProxies)
                .build()
            
        } catch (e: Exception) {
            logger.error("Error listing proxies", e)
            throw e
        }
    }
    
    override suspend fun getControllerStatus(request: ProxyManagementServiceOuterClass.GetControllerStatusRequest): ProxyControl.ControllerStatus {
        try {
            return controller.getControllerStatus()
            
        } catch (e: Exception) {
            logger.error("Error getting controller status", e)
            throw e
        }
    }
    
    override suspend fun deleteProxy(request: ProxyManagementServiceOuterClass.DeleteProxyRequest): CommonTypes.Empty {
        try {
            logger.info("Deleting proxy: ${request.proxyId}")
            
            val proxyInfo = controller.getProxy(request.proxyId)
            if (proxyInfo == null) {
                throw IllegalArgumentException("Proxy '${request.proxyId}' not found")
            }
            
            // Если прокси запущен, сначала останавливаем его
            if (proxyInfo.status == ProxyControl.ProxyStatus.PROXY_STATUS_RUNNING) {
                val stopRequest = ProxyManagementServiceOuterClass.StopProxyRequest.newBuilder()
                    .setProxyId(request.proxyId)
                    .build()
                stopProxy(stopRequest)
            }
            
            // Удаляем прокси из реестра
            controller.removeProxy(request.proxyId)
            
            logger.info("Proxy ${request.proxyId} deleted successfully")
            
            return CommonTypes.Empty.getDefaultInstance()
            
        } catch (e: Exception) {
            logger.error("Error deleting proxy ${request.proxyId}", e)
            throw e
        }
    }
    
    /**
     * Запуск процесса mitmproxy
     */
    private fun startMitmproxyProcess(proxyInfo: ProxyControl.ProxyInfo): Process? {
        try {
            // Путь к аддону
            val addonPath = File("mitmproxy_addon/proxiya_addon.py").absolutePath
            
            // Команда для запуска mitmproxy
            val command = mutableListOf(
                "mitmdump",
                "-s", addonPath,
                "-p", proxyInfo.port.toString(),
                "--set", "proxiya_proxy_id=${proxyInfo.proxyId}",
                "--set", "proxiya_controller_host=localhost",
                "--set", "proxiya_controller_port=50051"
            )
            
            logger.info("Starting mitmproxy with command: ${command.joinToString(" ")}")
            
            val processBuilder = ProcessBuilder(command)
            processBuilder.directory(File("."))
            
            // Перенаправляем вывод в логи
            processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE)
            processBuilder.redirectError(ProcessBuilder.Redirect.PIPE)
            
            val process = processBuilder.start()
            
            // Проверяем, что процесс запустился успешно
            if (process.isAlive) {
                logger.info("mitmproxy process started successfully (PID: ${process.pid()})")
                return process
            } else {
                logger.error("mitmproxy process failed to start")
                return null
            }
            
        } catch (e: Exception) {
            logger.error("Error starting mitmproxy process", e)
            return null
        }
    }
    
    /**
     * Остановка процесса mitmproxy
     */
    private fun stopMitmproxyProcess(proxyId: String): Boolean {
        try {
            // Получаем процесс из реестра
            val process = controller.getProxyProcess(proxyId)
            if (process == null) {
                logger.warn("No process found for proxy $proxyId")
                return true // Считаем успешным, если процесса нет
            }
            
            logger.info("Stopping mitmproxy process for proxy $proxyId (PID: ${process.pid()})")
            
            // Пытаемся остановить процесс gracefully
            process.destroy()
            
            // Ждем завершения процесса
            val terminated = process.waitFor(5, TimeUnit.SECONDS)
            
            if (terminated) {
                logger.info("mitmproxy process stopped gracefully")
                controller.removeProxyProcess(proxyId)
                return true
            } else {
                logger.warn("mitmproxy process did not stop gracefully, forcing...")
                process.destroyForcibly()
                
                // Ждем еще немного
                val forceTerminated = process.waitFor(2, TimeUnit.SECONDS)
                if (forceTerminated) {
                    logger.info("mitmproxy process force stopped")
                    controller.removeProxyProcess(proxyId)
                    return true
                } else {
                    logger.error("Failed to stop mitmproxy process")
                    return false
                }
            }
            
        } catch (e: Exception) {
            logger.error("Error stopping mitmproxy process for proxy $proxyId", e)
            return false
        }
    }
}
