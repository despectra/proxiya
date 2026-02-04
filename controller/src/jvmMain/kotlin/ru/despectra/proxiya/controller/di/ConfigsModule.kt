package ru.despectra.proxiya.controller.di

import org.koin.dsl.module
import ru.despectra.proxiya.controller.config.general.GeneralConfigRepository
import ru.despectra.proxiya.controller.config.proxy.ProxiesListConfigRepository
import ru.despectra.proxiya.controller.config.proxy.ProxyConfigMapper

fun configsModule() = module {
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