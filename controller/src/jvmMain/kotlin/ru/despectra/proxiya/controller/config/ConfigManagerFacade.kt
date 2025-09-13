package ru.despectra.proxiya.controller.config

import ru.despectra.proxiya.controller.config.general.GeneralConfigRepository
import ru.despectra.proxiya.controller.config.proxies.RegisteredProxiesConfigRepository

/**
 * Фасад для работы со всеми конфигурационными репозиториями.
 * Предоставляет единую точку входа для управления конфигурацией
 */
class ConfigManagerFacade {
    val generalConfig = GeneralConfigRepository()

    /**
     * Конфиги всех созданных проксей
     */
    val registeredProxiesConfig = RegisteredProxiesConfigRepository()
}
