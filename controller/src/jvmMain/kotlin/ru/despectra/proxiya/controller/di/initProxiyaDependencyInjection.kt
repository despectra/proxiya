package ru.despectra.proxiya.controller.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

fun initProxiyaDependencyInjection(): KoinApplication {
    return startKoin {
        printLogger(Level.DEBUG)

        modules(
            configsModule(),
            coreModule(),
            grpcModule(),
        )
    }
}