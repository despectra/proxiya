package ru.despectra.proxiya.controller

import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory
import ru.despectra.proxiya.proto.common.CommonTypes
import ru.despectra.proxiya.proto.control.ProxyControl
import ru.despectra.proxiya.proto.data.HttpData
import ru.despectra.proxiya.proto.data.ProxyDataServiceGrpcKt

/**
 * Реализация gRPC сервиса для передачи данных от mitmproxy аддонов
 */
class ProxyDataServiceImpl(
    private val proxyRepository: ProxyRepository,
) : ProxyDataServiceGrpcKt.ProxyDataServiceCoroutineImplBase() {

    interface ProxyRepository {
        fun incrementRequestCount()
        fun incrementResponseCount()
        fun updateProxy(proxyId: String, update: (ProxyControl.ProxyInfo) -> ProxyControl.ProxyInfo)
    }

    private val logger = LoggerFactory.getLogger(ProxyDataServiceImpl::class.java)
    
    override suspend fun sendRequest(request: HttpData.HttpRequest): CommonTypes.Empty {
        try {
            logger.debug("Received HTTP request from proxy ${request.proxyId}: ${request.method} ${request.url}")
            
            // Обновляем статистику
            proxyRepository.incrementRequestCount()
            
            // Обновляем время последней активности прокси
            proxyRepository.updateProxy(request.proxyId) { proxyInfo ->
                proxyInfo.toBuilder()
                    .setLastUpdated(System.currentTimeMillis())
                    .build()
            }
            
            // TODO: Здесь можно добавить обработку запроса
            // Например, сохранение в базу данных, фильтрация, модификация и т.д.
            
            logger.debug("Processed HTTP request from proxy ${request.proxyId}")
            
        } catch (e: Exception) {
            logger.error("Error processing HTTP request from proxy ${request.proxyId}", e)
            throw e
        }
        
        return CommonTypes.Empty.getDefaultInstance()
    }
    
    override suspend fun sendResponse(request: HttpData.HttpResponse): CommonTypes.Empty {
        try {
            logger.debug("Received HTTP response from proxy ${request.proxyId}: ${request.statusCode}")
            
            // Обновляем статистику
            proxyRepository.incrementResponseCount()
            
            // Обновляем время последней активности прокси
            proxyRepository.updateProxy(request.proxyId) { proxyInfo ->
                proxyInfo.toBuilder()
                    .setLastUpdated(System.currentTimeMillis())
                    .build()
            }
            
            // TODO: Здесь можно добавить обработку ответа
            // Например, сохранение в базу данных, анализ содержимого и т.д.
            
            logger.debug("Processed HTTP response from proxy ${request.proxyId}")
            
        } catch (e: Exception) {
            logger.error("Error processing HTTP response from proxy ${request.proxyId}", e)
            throw e
        }
        
        return CommonTypes.Empty.getDefaultInstance()
    }
    
    override suspend fun streamRequests(requests: Flow<HttpData.HttpRequest>): CommonTypes.Empty {
        logger.info("Starting stream requests")
        
        try {
            requests.collect { request ->
                try {
                    logger.debug("Received streamed HTTP request from proxy ${request.proxyId}: ${request.method} ${request.url}")
                    
                    // Обновляем статистику
                    proxyRepository.incrementRequestCount()
                    
                    // Обновляем время последней активности прокси
                    proxyRepository.updateProxy(request.proxyId) { proxyInfo ->
                        proxyInfo.toBuilder()
                            .setLastUpdated(System.currentTimeMillis())
                            .build()
                    }
                    
                    // TODO: Обработка запроса
                    
                } catch (e: Exception) {
                    logger.error("Error processing streamed HTTP request from proxy ${request.proxyId}", e)
                }
            }
            
            logger.debug("Completed stream requests processing")
            
        } catch (e: Exception) {
            logger.error("Error in stream requests", e)
            throw e
        }
        
        return CommonTypes.Empty.getDefaultInstance()
    }
    
    override suspend fun streamResponses(requests: Flow<HttpData.HttpResponse>): CommonTypes.Empty {
        logger.info("Starting stream responses")
        
        try {
            requests.collect { response ->
                try {
                    logger.debug("Received streamed HTTP response from proxy ${response.proxyId}: ${response.statusCode}")
                    
                    // Обновляем статистику
                    proxyRepository.incrementResponseCount()
                    
                    // Обновляем время последней активности прокси
                    proxyRepository.updateProxy(response.proxyId) { proxyInfo ->
                        proxyInfo.toBuilder()
                            .setLastUpdated(System.currentTimeMillis())
                            .build()
                    }
                    
                    // TODO: Обработка ответа
                    
                } catch (e: Exception) {
                    logger.error("Error processing streamed HTTP response from proxy ${response.proxyId}", e)
                }
            }
            
            logger.debug("Completed stream responses processing")
            
        } catch (e: Exception) {
            logger.error("Error in stream responses", e)
            throw e
        }
        
        return CommonTypes.Empty.getDefaultInstance()
    }
}
