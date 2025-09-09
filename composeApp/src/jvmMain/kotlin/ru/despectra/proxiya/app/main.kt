package ru.despectra.proxiya.app

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import org.koin.core.context.startKoin
import org.koin.core.logger.PrintLogger

fun main() = application {
    startKoin {
        logger(PrintLogger())
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Proxiya",
        state = WindowState(size = DpSize(1080.dp, 800.dp)),
    ) {
        App()
    }
}