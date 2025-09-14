package ru.despectra.proxiya.app.service

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ru.despectra.proxiya.proto.control.ProxyManagementServiceOuterClass.ProxiesListState
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Сервис для подписки на список проксей.
 * Обеспечивает переиспользование одного gRPC-стрима между множеством подписчиков
 */
class SharedProxiesStreamService(
    private val proxyControlService: ProxyControlService
) {
    private val logger = Logger.getLogger(SharedProxiesStreamService::class.java.name)

    // Общий поток для всех подписчиков
    private val _proxiesFlow = MutableSharedFlow<ProxiesListState>(
        replay = 1,
        extraBufferCapacity = 64
    )
    val proxiesFlow: SharedFlow<ProxiesListState> = _proxiesFlow.asSharedFlow()

    // Канал для управления подписками
    private val subscriptionChannel = Channel<SubscriptionCommand>(Channel.UNLIMITED)
    private var streamJob: Job? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Счетчик подписчиков управляется через канал команд, поэтому не атомик
    private var subscribersCount = 0
    
    init {
        serviceScope.launch {
            handleSubscriptionCommands()
        }
    }

    /**
     * Подписывается на поток данных о прокси.
     * Автоматически запускает gRPC-стрим при первом подписчике.
     */
    suspend fun subscribeToProxies(): Flow<ProxiesListState> {
        subscriptionChannel.send(SubscriptionCommand.Subscribe)
        return proxiesFlow
    }
    
    /**
     * Отписывается от потока данных о прокси.
     * Автоматически останавливает gRPC-стрим при отсутствии подписчиков.
     */
    suspend fun unsubscribeFromProxies() {
        subscriptionChannel.send(SubscriptionCommand.Unsubscribe)
    }
    
    /**
     * Обрабатывает команды подписки/отписки в одном потоке
     */
    private suspend fun handleSubscriptionCommands() {
        for (command in subscriptionChannel) {
            when (command) {
                is SubscriptionCommand.Subscribe -> {
                    subscribersCount++
                    logger.info("New subscriber added. Total subscribers: $subscribersCount")
                    
                    if (subscribersCount == 1) {
                        startStream()
                    }
                }
                is SubscriptionCommand.Unsubscribe -> {
                    subscribersCount = maxOf(0, subscribersCount - 1)
                    logger.info("Subscriber removed. Total subscribers: $subscribersCount")
                    
                    if (subscribersCount == 0) {
                        stopStream()
                    }
                }
                is SubscriptionCommand.Restart -> {
                    logger.info("Restarting stream. Current subscribers: $subscribersCount")
                    stopStream()
                    if (subscribersCount > 0) {
                        startStream()
                    }
                }
                is SubscriptionCommand.GetCount -> {
                    command.deferred.complete(subscribersCount)
                }
            }
        }
    }
    
    /**
     * Запускает gRPC-стрим и начинает передачу данных подписчикам
     */
    private fun startStream() {
        if (streamJob?.isActive == true) return
        
        logger.info("Starting shared proxies stream")
        
        streamJob = serviceScope.launch {
            try {
                proxyControlService.streamProxies().collect { proxiesList ->
                    _proxiesFlow.tryEmit(proxiesList)
                }
            } catch (e: Exception) {
                logger.log(Level.SEVERE, "Error in shared proxies stream", e)
                // При ошибке отправляем команду на перезапуск через канал
                launch {
                    delay(5000) // Ждем 5 секунд
                    subscriptionChannel.send(SubscriptionCommand.Restart)
                }
            } finally {
                logger.info("Shared proxies stream stopped")
            }
        }
    }
    
    /**
     * Останавливает gRPC стрим
     */
    private fun stopStream() {
        streamJob?.let { job ->
            if (job.isActive) {
                logger.info("Stopping shared proxies stream")
                job.cancel()
                streamJob = null
            }
        }
    }
    
    /**
     * Принудительно перезапускает стрим
     */
    suspend fun restartStream() {
        subscriptionChannel.send(SubscriptionCommand.Restart)
    }
    
    /**
     * Получает количество активных подписчиков
     */
    suspend fun getSubscribersCount(): Int {
        val deferred = CompletableDeferred<Int>()
        subscriptionChannel.send(SubscriptionCommand.GetCount(deferred))
        return deferred.await()
    }
    
    /**
     * Проверяет, активен ли стрим
     */
    fun isStreamActive(): Boolean = streamJob?.isActive == true
    
    /**
     * Освобождает ресурсы
     */
    fun close() {
        logger.info("Closing SharedProxiesStreamService")
        serviceScope.cancel()
        subscribersCount = 0
        streamJob = null
        subscriptionChannel.close()
    }
}

/**
 * Команды для управления подписками
 */
private sealed class SubscriptionCommand {
    object Subscribe : SubscriptionCommand()
    object Unsubscribe : SubscriptionCommand()
    object Restart : SubscriptionCommand()
    data class GetCount(val deferred: CompletableDeferred<Int>) : SubscriptionCommand()
}
