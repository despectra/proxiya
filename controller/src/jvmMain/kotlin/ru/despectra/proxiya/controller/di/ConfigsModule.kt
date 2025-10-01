package ru.despectra.proxiya.controller.di

import org.koin.core.KoinApplication
import org.koin.dsl.module
import ru.despectra.proxiya.controller.config.general.GeneralConfigRepository
import ru.despectra.proxiya.controller.config.proxies.ProxyConfigMapper
import ru.despectra.proxiya.controller.config.proxies.ProxyConfigRepository
import ru.despectra.proxiya.controller.config.proxies.RegisteredProxiesConfigRepository
import java.nio.file.Path

fun KoinApplication.configsModule() {
    module {
        factory<GeneralConfigRepository> {
            GeneralConfigRepository()
        }

        factory<RegisteredProxiesConfigRepository> {
            RegisteredProxiesConfigRepository()
        }

        factory<ProxyConfigRepository> { (filePath: Path) ->
            ProxyConfigRepository(filePath)
        }

        factory<ProxyConfigMapper> {
            ProxyConfigMapper()
        }
    }
}