package ru.despectra.proxiya.controller.config.proxies

import org.slf4j.LoggerFactory
import ru.despectra.proxiya.controller.config.YamlConfigRepository
import java.nio.file.Path

class RegisteredProxiesConfigRepository : YamlConfigRepository<RegisteredProxiesConfig>(
    fileName = "registered-proxies.yaml",
    serializer = RegisteredProxiesConfig.serializer(),
    defaultConfig = RegisteredProxiesConfig.default(),
) {
    private val logger = LoggerFactory.getLogger(RegisteredProxiesConfigRepository::class.java)
    private val proxyConfigsDir: Path = configManager.configDir.resolve("proxies")

    init {
        if (!proxyConfigsDir.toFile().exists()) {
            logger.info("Proxies configs directory is missing: $proxyConfigsDir")
            logger.info("Creating directory: $proxyConfigsDir")
            proxyConfigsDir.toFile().mkdirs()
        }
    }

    fun loadAll(): List<ProxyConfig> {
        val registeredProxiesConfig = load() ?: return emptyList()
        return registeredProxiesConfig.registeredProxies.mapNotNull {
            val path = configManager.configDir.resolve(it.value)
            configManager.loadConfig(path, ProxyConfig.serializer())
        }
    }

    fun loadById(proxyId: String): ProxyConfig? {
        val registeredProxiesConfig = load() ?: return null
        val configPath = registeredProxiesConfig.registeredProxies[proxyId]?.let {
            // относительно рутовой директории с конфигами
            configManager.configDir.resolve(it)
        } ?: return null
        return configManager.loadConfig(configPath, ProxyConfig.serializer())
    }

    fun saveProxyConfig(config: ProxyConfig) {
        val proxyConfigFilename = buildProxyConfigFilename(config)
        val proxyConfigPath = proxyConfigsDir.resolve(proxyConfigFilename)
        configManager.saveConfig(proxyConfigPath, ProxyConfig.serializer(), config)

        val registeredProxiesConfig = load() ?: return
        val registeredProxies = registeredProxiesConfig.registeredProxies
        val configRelativeDir = configManager.configDir.relativize(proxyConfigPath).toString()
        save(
            registeredProxiesConfig.copy(
                registeredProxies = registeredProxies + (config.id to configRelativeDir)
            )
        )
    }

    fun removeProxyConfig(proxyId: String) {
        val registeredProxiesConfig = load() ?: return
        val registeredProxies = registeredProxiesConfig.registeredProxies
        val configPath = registeredProxies[proxyId]?.let {
            configManager.configDir.resolve(it)
        } ?: return
        configPath.toFile().delete()

        save(
            registeredProxiesConfig.copy(
                registeredProxies = registeredProxies - proxyId
            )
        )
    }
    
    private fun buildProxyConfigFilename(config: ProxyConfig): String {
        return "${config.name.replace("\\s+".toRegex(), "_")}_${config.id}.yaml"
    }
}