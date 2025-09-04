package ru.despectra.proxiya.controller

import org.slf4j.LoggerFactory
import ru.despectra.proxiya.proto.common.CommonTypes
import ru.despectra.proxiya.proto.control.ProxyControl
import ru.despectra.proxiya.proto.control.ProxyNotificationServiceGrpcKt
import ru.despectra.proxiya.proto.control.ProxyNotificationServiceOuterClass

/**
 * Реализация gRPC сервиса для уведомлений от mitmproxy аддонов
 */
class ProxyNotificationServiceImpl(
    private val controller: ProxiyaController
) : ProxyNotificationServiceGrpcKt.ProxyNotificationServiceCoroutineImplBase() {
    
    private val logger = LoggerFactory.getLogger(ProxyNotificationServiceImpl::class.java)
    
    override suspend fun notifyProxyStarted(request: ProxyNotificationServiceOuterClass.NotifyProxyStartedRequest): CommonTypes.Empty {
        try {
            logger.info("Proxy ${request.proxyId} started (PID: ${request.pid})")
            
            // Обновляем информацию о прокси
            controller.updateProxy(request.proxyId) { proxyInfo ->
                proxyInfo.toBuilder()
                    .setStatus(ProxyControl.ProxyStatus.PROXY_STATUS_RUNNING)
                    .setPid(request.pid)
                    .setLastUpdated(System.currentTimeMillis())
                    .setErrorMessage(null)
                    .build()
            }
            
            logger.info("Proxy ${request.proxyId} is now running")
            
        } catch (e: Exception) {
            logger.error("Error processing proxy started notification for ${request.proxyId}", e)
            throw e
        }
        
        return CommonTypes.Empty.getDefaultInstance()
    }
    
    override suspend fun notifyProxyStopped(request: ProxyNotificationServiceOuterClass.NotifyProxyStoppedRequest): CommonTypes.Empty {
        try {
            logger.info("Proxy ${request.proxyId} stopped (exit code: ${request.exitCode})")
            
            // Обновляем информацию о прокси
            controller.updateProxy(request.proxyId) { proxyInfo ->
                proxyInfo.toBuilder()
                    .setStatus(ProxyControl.ProxyStatus.PROXY_STATUS_STOPPED)
                    .setLastUpdated(System.currentTimeMillis())
                    .setErrorMessage(request.reason)
                    .build()
            }
            
            // Удаляем процесс из реестра
            controller.removeProxyProcess(request.proxyId)
            
            logger.info("Proxy ${request.proxyId} is now stopped")
            
        } catch (e: Exception) {
            logger.error("Error processing proxy stopped notification for ${request.proxyId}", e)
            throw e
        }
        
        return CommonTypes.Empty.getDefaultInstance()
    }
    
    override suspend fun notifyProxyError(request: ProxyNotificationServiceOuterClass.NotifyProxyErrorRequest): CommonTypes.Empty {
        try {
            logger.error("Proxy ${request.proxyId} error: ${request.errorMessage} (type: ${request.errorType})")
            
            // Обновляем информацию о прокси
            controller.updateProxy(request.proxyId) { proxyInfo ->
                proxyInfo.toBuilder()
                    .setStatus(ProxyControl.ProxyStatus.PROXY_STATUS_ERROR)
                    .setLastUpdated(System.currentTimeMillis())
                    .setErrorMessage("${request.errorType}: ${request.errorMessage}")
                    .build()
            }
            
            // Логируем дополнительные детали ошибки
            if (request.errorDetailsCount > 0) {
                logger.error("Error details for proxy ${request.proxyId}:")
                request.errorDetailsMap.forEach { (key, value) ->
                    logger.error("  $key: $value")
                }
            }
            
        } catch (e: Exception) {
            logger.error("Error processing proxy error notification for ${request.proxyId}", e)
            throw e
        }
        
        return CommonTypes.Empty.getDefaultInstance()
    }
    
    override suspend fun heartbeat(request: ProxyNotificationServiceOuterClass.HeartbeatRequest): CommonTypes.Empty {
        try {
            logger.debug("Heartbeat from proxy ${request.proxyId}")
            
            // Обновляем время последней активности прокси
            controller.updateProxy(request.proxyId) { proxyInfo ->
                proxyInfo.toBuilder().setLastUpdated(System.currentTimeMillis()).build()
            }
            
            // Обрабатываем статистику, если она предоставлена
            if (request.hasStats()) {
                val stats = request.stats
                logger.debug("Proxy ${request.proxyId} stats: " +
                    "requests=${stats.requestsProcessed}, " +
                    "responses=${stats.responsesProcessed}, " +
                    "last_activity=${stats.lastActivity}")
                
                // TODO: Можно сохранить статистику в базу данных или использовать для мониторинга
            }
            
        } catch (e: Exception) {
            logger.error("Error processing heartbeat from proxy ${request.proxyId}", e)
            throw e
        }
        
        return CommonTypes.Empty.getDefaultInstance()
    }
}
