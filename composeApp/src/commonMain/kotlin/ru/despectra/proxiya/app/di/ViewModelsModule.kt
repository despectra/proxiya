package ru.despectra.proxiya.app.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.despectra.proxiya.app.ui.viewmodel.AppNavigationViewModel
import ru.despectra.proxiya.app.ui.viewmodel.CreateProxyViewModel
import ru.despectra.proxiya.app.ui.viewmodel.ProxyMainViewModel
import ru.despectra.proxiya.app.ui.viewmodel.SettingsViewModel
import ru.despectra.proxiya.app.ui.viewmodel.SidebarViewModel
import ru.despectra.proxiya.app.ui.viewmodel.ThemeViewModel

@Deprecated("Use decompose")
fun viewModelsModule() = module {
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