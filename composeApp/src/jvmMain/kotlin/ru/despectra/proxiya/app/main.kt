package ru.despectra.proxiya.app

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import org.koin.core.context.startKoin
import org.koin.core.logger.PrintLogger
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.dsl.onClose
import ru.despectra.proxiya.app.data.ProxiesRepository
import ru.despectra.proxiya.app.service.ProxyControlService
import ru.despectra.proxiya.app.viewmodel.AppNavigationViewModel
import ru.despectra.proxiya.app.viewmodel.SidebarViewModel

fun main() = application {
    val koinApp = startKoin {
        logger(PrintLogger())

        modules(
            // Services
            module {
                single<ProxyControlService> {
                    ProxyControlService().apply {
                        initialize(host = "localhost", port = 50051)
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
            },

            // ViewModels
            module {
                viewModel<AppNavigationViewModel> {
                    AppNavigationViewModel()
                }
                viewModel<SidebarViewModel> {
                    SidebarViewModel(proxiesRepository = get())
                }
            }
        )
    }

    Window(
        onCloseRequest = {
            exitApplication()
            koinApp.close()
        },
        title = "Proxiya",
        state = WindowState(size = DpSize(1080.dp, 800.dp)),
    ) {
        App()
    }
}