package ru.despectra.proxiya.app

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import ru.despectra.proxiya.app.di.initDependencyInjectionContainer

fun main() = application {
    val dependencyInjectionContainer = initDependencyInjectionContainer()

    Window(
        onCloseRequest = {
            exitApplication()
            dependencyInjectionContainer.close()
        },
        title = "Proxiya",
        state = WindowState(size = DpSize(1080.dp, 800.dp)),
    ) {
        App()
    }
}