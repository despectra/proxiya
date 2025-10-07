package ru.despectra.proxiya.app.di

import org.koin.dsl.module
import ru.despectra.proxiya.app.component.pane.PaneChildFactory
import ru.despectra.proxiya.app.component.pane.proxy.create.CreateProxyComponent
import ru.despectra.proxiya.app.component.pane.proxy.create.DefaultCreateProxyComponent
import ru.despectra.proxiya.app.component.pane.proxy.main.ProxyMainPaneComponent
import ru.despectra.proxiya.app.component.pane.proxy.main.ProxyMainPaneComponentImpl
import ru.despectra.proxiya.app.component.pane.settings.DefaultSettingsComponent
import ru.despectra.proxiya.app.component.pane.settings.SettingsComponent
import ru.despectra.proxiya.app.component.root.AppComponent
import ru.despectra.proxiya.app.component.root.DefaultAppComponent
import ru.despectra.proxiya.app.component.sidebar.DefaultSidebarComponent
import ru.despectra.proxiya.app.component.sidebar.SidebarComponent

val componentsModule = module {
    factory<AppComponent.Factory> {
        DefaultAppComponent.Factory(
            appSettingsRepository = get(),
            sidebarComponentFactory = get(),
            paneChildFactory = get(),
        )
    }

    factory<SidebarComponent.Factory> {
        DefaultSidebarComponent.Factory(
            proxiesRepository = get(),
        )
    }

    factory<PaneChildFactory> {
        PaneChildFactory(
            createProxyComponentFactory = get(),
            settingsComponentFactory = get(),
            proxyMainPaneComponentFactory = get(),
        )
    }

    factory<CreateProxyComponent.Factory> {
        DefaultCreateProxyComponent.Factory(
            proxiesRepository = get(),
        )
    }

    factory<SettingsComponent.Factory> {
        DefaultSettingsComponent.Factory(
            appSettingsRepository = get(),
        )
    }

    factory<ProxyMainPaneComponent.Factory> {
        ProxyMainPaneComponentImpl.Factory(
            proxiesRepository = get(),
        )
    }
}