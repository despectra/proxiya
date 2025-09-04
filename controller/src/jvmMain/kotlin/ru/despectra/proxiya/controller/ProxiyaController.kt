package ru.despectra.proxiya.controller

import io.grpc.BindableService
import io.grpc.Server
import io.grpc.ServerBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import ru.despectra.proxiya.proto.control.ProxyControl
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Основной контроллер Proxiya
 * 
 * Управляет прокси, принимает данные от mitmproxy аддонов,
 * предоставляет gRPC API для взаимодействия
 */
class ProxiyaController(
    private val port: Int = 50051,
    private val host: String = "0.0.0.0"
) {
    private val logger = LoggerFactory.getLogger(ProxiyaController::class.java)
    
    private var server: Server? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Хранилище прокси
    private val proxies = ConcurrentHashMap<String, ProxyControl.ProxyInfo>()
    private val proxyProcesses = ConcurrentHashMap<String, Process>()
    
    // Статистика
    private val totalRequests = AtomicInteger(0)
    private val totalResponses = AtomicInteger(0)
    
    // gRPC сервисы
    private lateinit var dataService: ProxyDataServiceImpl
    private lateinit var notificationService: ProxyNotificationServiceImpl
    private lateinit var managementService: ProxyManagementServiceImpl
    
    /**
     * Запуск контроллера
     */
    suspend fun start() {
        logger.info("Starting Proxiya Controller on $host:$port")
        
        // Инициализируем сервисы
        dataService = ProxyDataServiceImpl(this)
        notificationService = ProxyNotificationServiceImpl(this)
        managementService = ProxyManagementServiceImpl(this)
        
        // Создаем gRPC сервер
        server = ServerBuilder.forPort(port)
            .addService(dataService as BindableService)
            .addService(notificationService as BindableService)
            .addService(managementService as BindableService)
            .build()
        
        // Запускаем сервер
        server?.start()
        
        logger.info("Proxiya Controller started successfully on port $port")
        
        // Запускаем фоновые задачи
        startBackgroundTasks()
    }
    
    /**
     * Остановка контроллера
     */
    suspend fun stop() {
        logger.info("Stopping Proxiya Controller...")
        
        // Останавливаем все прокси
        stopAllProxies()
        
        // Останавливаем gRPC сервер
        server?.shutdown()
        server?.awaitTermination()
        
        // Отменяем корутины
        scope.cancel()
        
        logger.info("Proxiya Controller stopped")
    }
    
    /**
     * Запуск фоновых задач
     */
    private fun startBackgroundTasks() {
        // Задача для мониторинга прокси
        scope.launch {
            while (isActive) {
                try {
                    monitorProxies()
                    delay(30_000) // Проверяем каждые 30 секунд
                } catch (e: Exception) {
                    logger.error("Error in proxy monitoring task", e)
                }
            }
        }
        
        // Задача для очистки старых данных
        scope.launch {
            while (isActive) {
                try {
                    cleanupOldData()
                    delay(300_000) // Очищаем каждые 5 минут
                } catch (e: Exception) {
                    logger.error("Error in cleanup task", e)
                }
            }
        }
    }
    
    /**
     * Мониторинг состояния прокси
     */
    private suspend fun monitorProxies() {
        val iterator = proxyProcesses.iterator()
        while (iterator.hasNext()) {
            val (proxyId, process) = iterator.next()
            
            if (!process.isAlive) {
                logger.warn("Proxy $proxyId process is not alive, removing from registry")
                
                // Обновляем статус прокси
                proxies[proxyId]?.let { proxyInfo ->
                    proxies[proxyId] = proxyInfo.toBuilder()
                        .setStatus(ProxyControl.ProxyStatus.PROXY_STATUS_STOPPED)
                        .setLastUpdated(System.currentTimeMillis())
                        .setErrorMessage("Process terminated unexpectedly")
                        .build()
                }
                
                iterator.remove()
            }
        }
    }
    
    /**
     * Очистка старых данных
     */
    private suspend fun cleanupOldData() {
        val now = System.currentTimeMillis()
        val cutoffTime = now - (24 * 60 * 60 * 1000) // 24 часа назад
        
        val iterator = proxies.iterator()
        while (iterator.hasNext()) {
            val (proxyId, proxyInfo) = iterator.next()
            
            if (proxyInfo.status == ProxyControl.ProxyStatus.PROXY_STATUS_STOPPED &&
                proxyInfo.lastUpdated < cutoffTime) {
                
                logger.debug("Removing old proxy $proxyId")
                iterator.remove()
            }
        }
    }
    
    /**
     * Остановка всех прокси
     */
    private suspend fun stopAllProxies() {
        logger.info("Stopping all proxies...")
        
        for ((proxyId, process) in proxyProcesses) {
            try {
                logger.info("Stopping proxy $proxyId")
                process.destroy()
                
                // Ждем завершения процесса
                if (process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    logger.info("Proxy $proxyId stopped successfully")
                } else {
                    logger.warn("Proxy $proxyId did not stop gracefully, forcing...")
                    process.destroyForcibly()
                }
                
                // Обновляем статус
                proxies[proxyId]?.let { proxyInfo ->
                    proxies[proxyId] = proxyInfo.toBuilder()
                        .setStatus(ProxyControl.ProxyStatus.PROXY_STATUS_STOPPED)
                        .setLastUpdated(System.currentTimeMillis())
                        .build()
                }
                
            } catch (e: Exception) {
                logger.error("Error stopping proxy $proxyId", e)
            }
        }
        
        proxyProcesses.clear()
    }
    
    // Методы для взаимодействия с сервисами
    
    fun getProxies(): Map<String, ProxyControl.ProxyInfo> = proxies.toMap()
    
    fun getProxy(proxyId: String): ProxyControl.ProxyInfo? = proxies[proxyId]
    
    fun addProxy(proxyInfo: ProxyControl.ProxyInfo) {
        proxies[proxyInfo.proxyId] = proxyInfo
        logger.info("Added proxy: ${proxyInfo.proxyId}")
    }
    
    fun removeProxy(proxyId: String) {
        proxies.remove(proxyId)
        logger.info("Removed proxy: $proxyId")
    }
    
    fun updateProxy(proxyId: String, update: (ProxyControl.ProxyInfo) -> ProxyControl.ProxyInfo) {
        proxies[proxyId]?.let { current ->
            val updated = update(current)
            proxies[proxyId] = updated
            logger.debug("Updated proxy: $proxyId")
        }
    }
    
    fun addProxyProcess(proxyId: String, process: Process) {
        proxyProcesses[proxyId] = process
        logger.info("Added proxy process: $proxyId (PID: ${process.pid()})")
    }
    
    fun removeProxyProcess(proxyId: String) {
        proxyProcesses.remove(proxyId)
        logger.info("Removed proxy process: $proxyId")
    }
    
    fun getProxyProcess(proxyId: String): Process? = proxyProcesses[proxyId]
    
    fun incrementRequestCount() = totalRequests.incrementAndGet()
    
    fun incrementResponseCount() = totalResponses.incrementAndGet()
    
    fun getTotalRequests() = totalRequests.get()
    
    fun getTotalResponses() = totalResponses.get()
    
    fun getControllerStatus(): ProxyControl.ControllerStatus {
        val activeProxies = proxies.values.count { 
            it.status == ProxyControl.ProxyStatus.PROXY_STATUS_RUNNING
        }
        
        return ProxyControl.ControllerStatus.newBuilder()
            .setState(ProxyControl.ControllerState.CONTROLLER_STATE_RUNNING)
            .setActiveProxiesCount(activeProxies)
            .addAllProxies(proxies.values)
            .setLastUpdated(System.currentTimeMillis())
            .setVersion("1.0.0")
            .build()
    }
}
