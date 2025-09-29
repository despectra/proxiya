package ru.despectra.proxiya.controller.config.proxies

import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import org.slf4j.LoggerFactory
import ru.despectra.proxiya.controller.config.YamlConfigRepository
import java.nio.file.Path
import java.nio.file.Paths

class RegisteredProxiesConfigRepository : YamlConfigRepository<RegisteredProxiesConfig>(
    filePath = Paths.get("registered-proxies.yaml"),
    serializer = RegisteredProxiesConfig.serializer(),
    defaultConfig = RegisteredProxiesConfig.default(),
), KoinComponent {
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
        return registeredProxiesConfig.registeredProxies.mapNotNull { (_, proxyConfigPath) ->
            val configPath = configManager.configDir.resolve(proxyConfigPath)
            get<ProxyConfigRepository> { parametersOf(configPath) }.load()
        }
    }

    fun loadById(proxyId: String): ProxyConfig? {
        val registeredProxiesConfig = load() ?: return null
        return registeredProxiesConfig.registeredProxies[proxyId]?.let {
            val configPath = configManager.configDir.resolve(it)
            return get<ProxyConfigRepository> { parametersOf(configPath) }.load()
        }
    }

    fun saveProxyConfig(config: ProxyConfig) {
        val registeredProxiesConfig = load() ?: return
        val registeredProxies = registeredProxiesConfig.registeredProxies
        val proxyConfigPath = registeredProxies[config.id]?.let {
            configManager.configDir.resolve(it)
        } ?: let {
            val newProxyConfigFilename = buildProxyConfigFilename(config)
            proxyConfigsDir.resolve(newProxyConfigFilename)
        }

        get<ProxyConfigRepository> { parametersOf(proxyConfigPath) }.save(config)

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