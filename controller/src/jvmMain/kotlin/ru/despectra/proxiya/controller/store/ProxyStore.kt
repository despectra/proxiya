package ru.despectra.proxiya.controller.store

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import ru.despectra.proxiya.controller.config.proxy.ProxyConfigMapper
import ru.despectra.proxiya.controller.config.proxy.ProxiesListConfig
import ru.despectra.proxiya.controller.config.proxy.ProxiesListConfigRepository
import ru.despectra.proxiya.controller.repository.ProxyId
import ru.despectra.proxiya.controller.utils.MutexByKey
import ru.despectra.proxiya.proto.proxy.ProxyInstance
import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class ProxyStore(
    private val proxiesListConfigRepository: ProxiesListConfigRepository,
    private val proxyConfigMapper: ProxyConfigMapper,
) : Closeable {
    private val proxiesMap = ConcurrentHashMap<ProxyId, ProxyInstance>()
    private val _proxiesListFlow = MutableStateFlow<List<ProxyInstance>>(emptyList())

    private val mutexByKey = MutexByKey<ProxyId>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val logger = LoggerFactory.getLogger(javaClass)

    val proxiesListFlow = _proxiesListFlow.asStateFlow()

    init {
        scope.launch {
            loadProxiesFromConfig()
            setupConfigAutosaveOnChanges()
        }
    }

    fun getProxy(proxyId: ProxyId): ProxyInstance? {
        return proxiesMap[proxyId]
    }

    suspend fun saveProxy(proxyInstance: ProxyInstance) {
        val proxyId = proxyInstance.proxyId
        mutexByKey[proxyId].withLock {
            proxiesMap[proxyId] = proxyInstance
            _proxiesListFlow.value = proxiesMap.values.toList()
        }
    }

    suspend fun updateProxy(proxyId: ProxyId, updater: ProxyInstance.() -> ProxyInstance) {
        mutexByKey[proxyId].withLock {
            proxiesMap[proxyId]?.let {
                proxiesMap[proxyId] = updater(it)
                _proxiesListFlow.value = proxiesMap.values.toList()
            }
        }
    }

    suspend fun removeProxy(proxyId: ProxyId) {
        mutexByKey[proxyId].withLock {
            mutexByKey.remove(proxyId)
            proxiesMap.remove(proxyId)
            _proxiesListFlow.value = proxiesMap.values.toList()
        }
    }

    private suspend fun loadProxiesFromConfig() {
        proxiesListConfigRepository.load()?.let { proxiesListConfig ->
            proxiesListConfig.proxies.forEach { (proxyId, proxyConfig) ->
                val proxyInstance = proxyConfigMapper.proxyInstanceFromConfig(proxyId, proxyConfig)
                saveProxy(proxyInstance)
            }
        }
    }

    private fun CoroutineScope.setupConfigAutosaveOnChanges() {
        _proxiesListFlow
            .debounce(SAVE_TO_CONFIG_DEBOUNCE_MS.milliseconds)
            .map { proxyInstanceList ->
                val proxyConfigs = proxyInstanceList.mapNotNull { proxyInstance ->
                    if (!proxyInstance.hasProxyId()) return@mapNotNull null
                    if (!proxyInstance.hasCreatedAt()) return@mapNotNull null
                    if (!proxyInstance.hasParams()) return@mapNotNull null
                    proxyConfigMapper.proxyConfigFromInstance(proxyInstance)?.let {
                        proxyInstance.proxyId to it
                    }
                }.sortedBy { (_, proxy) ->
                    proxy.createdAt
                }
                ProxiesListConfig(proxies = LinkedHashMap(proxyConfigs.toMap()))
            }
            .distinctUntilChanged()
            .onEach {
                proxiesListConfigRepository.save(it)
                logger.debug("Proxies list config saved")
            }
            .catch { logger.error("Error autosaving proxies list config", it) }
            .launchIn(this)
    }

    override fun close() {
        scope.cancel()
        mutexByKey.clear()
    }

    companion object {
        private const val SAVE_TO_CONFIG_DEBOUNCE_MS = 1000L
    }
}