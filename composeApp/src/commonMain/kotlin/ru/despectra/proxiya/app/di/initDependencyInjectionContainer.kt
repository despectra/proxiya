package ru.despectra.proxiya.app.di

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.dsl.onClose
import ru.despectra.proxiya.app.data.proxy.ProxiesRepository
import ru.despectra.proxiya.app.data.settings.AppSettingsRepository
import ru.despectra.proxiya.app.service.ProxyControlService
import ru.despectra.proxiya.app.ui.viewmodel.AppNavigationViewModel
import ru.despectra.proxiya.app.ui.viewmodel.CreateProxyViewModel
import ru.despectra.proxiya.app.ui.viewmodel.ProxyMainViewModel
import ru.despectra.proxiya.app.ui.viewmodel.SettingsViewModel
import ru.despectra.proxiya.app.ui.viewmodel.SidebarViewModel
import ru.despectra.proxiya.app.ui.viewmodel.ThemeViewModel
import java.util.prefs.Preferences

internal fun initDependencyInjectionContainer(): KoinApplication {
    return startKoin {
        printLogger(level = Level.DEBUG)

        modules(
            // Services
            module {
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
            },

            // Repositories
            module {
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
            },

            // ViewModels
            module {
                viewModel<AppNavigationViewModel> {
                    AppNavigationViewModel()
                }
                viewModel<SidebarViewModel> {
                    SidebarViewModel(proxiesRepository = get())
                }
                viewModel<ThemeViewModel> {
                    ThemeViewModel(appSettingsRepository = get())
                }
                viewModel<SettingsViewModel> {
                    SettingsViewModel(appSettingsRepository = get())
                }
                viewModel<CreateProxyViewModel> {
                    CreateProxyViewModel(proxiesRepository = get())
                }
                viewModel<ProxyMainViewModel> { (proxyId: String) ->
                    ProxyMainViewModel(
                        proxyId = proxyId,
                        proxiesRepository = get()
                    )
                }
            }
        )
    }
}