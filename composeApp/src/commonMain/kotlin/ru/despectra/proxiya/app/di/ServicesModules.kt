package ru.despectra.proxiya.app.di

import org.koin.dsl.module
import org.koin.dsl.onClose
import ru.despectra.proxiya.app.service.ProxyControlService

val servicesModules = module {
    single<ProxyControlService> {
        ProxyControlService(
            ProxyControlService.Config(
                host = "localhost",
                port = 50051,
            )
        ).apply {
            initialize()
        }
    } onClose { it?.close() }
}