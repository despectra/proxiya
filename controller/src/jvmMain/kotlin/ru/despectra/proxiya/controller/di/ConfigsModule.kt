package ru.despectra.proxiya.controller.di

import org.koin.core.KoinApplication
import org.koin.dsl.module
import ru.despectra.proxiya.controller.config.general.GeneralConfigRepository
import ru.despectra.proxiya.controller.config.proxy.ProxyConfigMapper
import ru.despectra.proxiya.controller.config.proxy.ProxiesListConfigRepository

fun KoinApplication.configsModule() = module {
    factory<GeneralConfigRepository> {
        GeneralConfigRepository()
    }

    factory<ProxiesListConfigRepository> {
        ProxiesListConfigRepository()
    }

    factory<ProxyConfigMapper> {
        ProxyConfigMapper()
    }
}