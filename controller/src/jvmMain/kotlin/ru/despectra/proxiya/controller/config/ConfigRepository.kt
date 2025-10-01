package ru.despectra.proxiya.controller.config

import kotlinx.serialization.KSerializer
import java.nio.file.Path

/**
 * Базовый интерфейс для работы с конфигурационными файлами
 */
interface ConfigRepository<T> {

    /**
     * Загружает конфигурацию из файла
     */
    fun load(): T?
    
    /**
     * Сохраняет конфигурацию в файл
     */
    fun save(config: T)
}

/**
 * Базовая реализация репозитория для YAML файлов
 *
 * @property filePath путь к файлу конфига, относительно рутовой директории с YAML-конфигами
 * @property serializer объект-сериалайзер для модели
 * @property defaultConfig значение по-умолчанию в случае отсутствия конфига
 */
abstract class YamlConfigRepository<T>(
    private val filePath: Path,
    private val serializer: KSerializer<T>,
    private val defaultConfig: T?
) : ConfigRepository<T> {
    
    protected val configManager = YamlConfigManager()
    
    override fun load(): T? {
        return configManager.loadConfig(
            relativeFilePath = configManager.configDir.resolve(filePath),
            serializer = serializer,
            defaultConfig = defaultConfig
        )
    }
    
    override fun save(config: T) {
        configManager.saveConfig(
            relativeFilePath = configManager.configDir.resolve(filePath),
            serializer = serializer,
            config = config
        )
    }
}
