package ru.despectra.proxiya.app.di

import org.koin.dsl.module
import ru.despectra.proxiya.app.component.root.AppComponent
import ru.despectra.proxiya.app.component.root.DefaultAppComponent
import ru.despectra.proxiya.app.component.sidebar.DefaultSidebarComponent
import ru.despectra.proxiya.app.component.sidebar.SidebarComponent

val componentsModule = module {
    factory<AppComponent.Factory> {
        DefaultAppComponent.Factory(
            appSettingsRepository = get(),
            sidebarComponentFactory = get(),
        )
    }

    factory<SidebarComponent.Factory> {
        DefaultSidebarComponent.Factory(
            proxiesRepository = get(),
        )
    }
}