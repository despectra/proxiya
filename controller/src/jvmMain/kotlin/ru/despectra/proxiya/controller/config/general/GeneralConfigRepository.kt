package ru.despectra.proxiya.controller.config.general

import ru.despectra.proxiya.controller.config.YamlConfigRepository
import java.nio.file.Paths

/**
 * Репозиторий для работы с общей конфигурацией
 */
class GeneralConfigRepository : YamlConfigRepository<GeneralConfig>(
    filePath = Paths.get("general-config.yaml"),
    serializer = GeneralConfig.serializer(),
    defaultConfig = GeneralConfig.default()
)
