package ru.despectra.proxiya.app.di

import org.koin.dsl.module
import ru.despectra.proxiya.app.component.pane.proxy.main.InterceptedRequestsFormatter
import ru.despectra.proxiya.app.component.pane.proxy.main.WaterfallTableFormatter

val formattersModule = module {
    factory<InterceptedRequestsFormatter> { InterceptedRequestsFormatter() }
    factory<WaterfallTableFormatter> { WaterfallTableFormatter() }
}