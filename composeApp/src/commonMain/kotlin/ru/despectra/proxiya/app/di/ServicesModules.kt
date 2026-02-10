package ru.despectra.proxiya.app.di

import org.koin.dsl.module
import org.koin.dsl.onClose
import ru.despectra.proxiya.app.service.GrpcConfig
import ru.despectra.proxiya.app.service.ProxyControlService
import ru.despectra.proxiya.app.service.ProxyDataService

val servicesModules = module {
    single<ProxyControlService> {
        ProxyControlService(
            config = get<GrpcConfig>()
        ).apply {
            initialize()
        }
    } onClose { it?.close() }

    single<ProxyDataService> {
        ProxyDataService(
            config = get<GrpcConfig>()
        ).apply {
            initialize()
        }
    } onClose { it?.close() }

    single<GrpcConfig> {
        GrpcConfig(
            host = "localhost",
            port = 50051
        )
    }
}