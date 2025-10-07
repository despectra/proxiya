package ru.despectra.proxiya.app.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

internal fun initDependencyInjectionContainer(): KoinApplication {
    return startKoin {
        printLogger(level = Level.DEBUG)

        modules(
            servicesModules,
            repositoriesModules,
            componentsModule,
        )
    }
}