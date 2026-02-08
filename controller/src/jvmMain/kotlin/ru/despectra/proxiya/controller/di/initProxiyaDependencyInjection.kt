package ru.despectra.proxiya.controller.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import ru.despectra.proxiya.controller.AppStartConfig

fun initProxiyaDependencyInjection(appStartConfig: AppStartConfig): KoinApplication {
    return startKoin {
        printLogger(Level.DEBUG)

        modules(
            configsModule(appStartConfig),
            coreModule(),
            grpcModule(),
        )
    }
}