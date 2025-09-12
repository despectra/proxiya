package ru.despectra.proxiya.controller.config

import ru.despectra.proxiya.controller.config.general.GeneralConfigRepository

/**
 * Фасад для работы со всеми конфигурационными репозиториями.
 * Предоставляет единую точку входа для управления конфигурацией
 */
class ConfigManagerFacade {
    val generalConfig = GeneralConfigRepository()
}
