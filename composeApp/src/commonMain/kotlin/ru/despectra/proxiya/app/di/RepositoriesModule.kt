package ru.despectra.proxiya.app.di

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import org.koin.dsl.module
import ru.despectra.proxiya.app.data.proxy.ProxiesRepository
import ru.despectra.proxiya.app.data.settings.AppSettingsRepository
import java.util.prefs.Preferences

val repositoriesModules = module {
    factory<ProxiesRepository> {
        ProxiesRepository(
            proxyControlService = get(),
        )
    }

    single<Settings> {
        PreferencesSettings.Factory(
            Preferences.userRoot(),
        ).create("ru.despectra.proxiya.app")
    }
    single<AppSettingsRepository> {
        AppSettingsRepository(settings = get())
    }
}