package ru.despectra.proxiya.controller.config.general

import ru.despectra.proxiya.controller.config.YamlConfigRepository

/**
 * Репозиторий для работы с общей конфигурацией
 */
class GeneralConfigRepository : YamlConfigRepository<GeneralConfig>(
    fileName = "general-config.yaml",
    serializer = GeneralConfig.serializer(),
    defaultConfig = GeneralConfig.default()
)
