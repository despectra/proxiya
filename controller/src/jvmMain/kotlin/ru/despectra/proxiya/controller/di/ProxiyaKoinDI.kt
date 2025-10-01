package ru.despectra.proxiya.controller.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

fun proxiyaKoinApp(): KoinApplication {
    return startKoin {
        // YAML Configs
        configsModule()
    }
}