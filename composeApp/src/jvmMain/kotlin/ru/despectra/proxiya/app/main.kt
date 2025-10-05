package ru.despectra.proxiya.app

import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import ru.despectra.proxiya.app.di.initDependencyInjectionContainer
import ru.despectra.proxiya.app.util.runOnUiThread

fun main() {
    val dependencyInjectionContainer = initDependencyInjectionContainer()

    val lifecycle = LifecycleRegistry()
    runOnUiThread {
        val defaultComponentContext = DefaultComponentContext(
            lifecycle = lifecycle
        )
    }

    return application {
        val windowState = rememberWindowState(size = DpSize(1080.dp, 800.dp))
        Window(
            onCloseRequest = {
                exitApplication()
                dependencyInjectionContainer.close()
            },
            title = "Proxiya",
            state = windowState,
        ) {
            LifecycleController(
                lifecycleRegistry = lifecycle,
                windowState = windowState,
                windowInfo = LocalWindowInfo.current,
            )

            App()
        }
    }
}