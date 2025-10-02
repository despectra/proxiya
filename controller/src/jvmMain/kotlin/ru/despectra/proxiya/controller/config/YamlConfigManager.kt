package ru.despectra.proxiya.controller.config

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Обобщенный менеджер для работы с YAML конфигурационными файлами
 */
class YamlConfigManager {
    private val logger = LoggerFactory.getLogger(YamlConfigManager::class.java)
    private val yaml = Yaml(
        serializersModule = SerializersModule {
            contextual(LocalDateTimeSerializer())
        }
    )
    val configDir: Path = getConfigDirectory()
    
    init {
        if (!Files.exists(configDir)) {
            Files.createDirectories(configDir)
            logger.info("Создана директория конфигурации: $configDir")
        }
    }
    
    /**
     * Загружает конфигурацию из YAML файла
     */
    fun <T> loadConfig(relativeFilePath: Path, serializer: KSerializer<T>, defaultConfig: T? = null): T? {
        val configFile = configDir.resolve(relativeFilePath)
        
        return if (Files.exists(configFile)) {
            try {
                val configContent = Files.readString(configFile)
                val config = yaml.decodeFromString(serializer, configContent)
                logger.info("Загружена конфигурация из файла: $configFile")
                config
            } catch (e: Exception) {
                logger.warn("Ошибка при загрузке конфигурации из $configFile: ${e.message}")
                logger.info("Используется конфигурация по умолчанию")
                defaultConfig
            }
        } else if (defaultConfig != null) {
            logger.info("Файл конфигурации не найден: $configFile")
            logger.info("Создается конфигурация по умолчанию")
            saveConfig(relativeFilePath, serializer, defaultConfig)
            defaultConfig
        } else {
            logger.info("Файл конфигурации не найден: $configFile, -> null")
            null
        }
    }
    
    /**
     * Сохраняет конфигурацию в YAML файл
     */
    fun <T> saveConfig(relativeFilePath: Path, serializer: KSerializer<T>, config: T) {
        val configFile = configDir.resolve(relativeFilePath)
        
        try {
            val yamlContent = yaml.encodeToString(serializer, config)
            Files.writeString(configFile, yamlContent)
            logger.info("Конфигурация сохранена в файл: $configFile")
        } catch (e: Exception) {
            logger.error("Ошибка при сохранении конфигурации в $configFile: ${e.message}", e)
            throw RuntimeException("Не удалось сохранить конфигурацию", e)
        }
    }
    
    /**
     * Получает директорию для хранения конфигурационных файлов
     * На macOS: ~/Library/Application Support/Proxiya/
     */
    private fun getConfigDirectory(): Path {
        val userHome = System.getProperty("user.home")
        return Paths.get(userHome, "Library", "Application Support", "Proxiya")
    }
}
