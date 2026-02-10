package ru.despectra.proxiya.app.di

import org.koin.dsl.module
import ru.despectra.proxiya.app.component.pane.proxy.main.InterceptedRequestsFormatter

val formattersModule = module {
    factory<InterceptedRequestsFormatter> { InterceptedRequestsFormatter() }
}