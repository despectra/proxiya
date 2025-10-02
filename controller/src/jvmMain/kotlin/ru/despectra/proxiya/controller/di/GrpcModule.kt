package ru.despectra.proxiya.controller.di

import org.koin.core.KoinApplication
import org.koin.dsl.module
import ru.despectra.proxiya.controller.grpc.ProxyControlServiceImpl
import ru.despectra.proxiya.controller.grpc.ProxyDataServiceImpl
import ru.despectra.proxiya.controller.grpc.instance.ProxyInstanceControlServiceImpl
import ru.despectra.proxiya.controller.grpc.instance.ProxyInstanceDataServiceImpl
import ru.despectra.proxiya.controller.repository.ProxyRepository

fun KoinApplication.grpcModule() = module {
    single<ProxyControlServiceImpl> {
        ProxyControlServiceImpl(
            proxyRepository = get<ProxyRepository>(),
        )
    }

    single<ProxyDataServiceImpl> {
        ProxyDataServiceImpl()
    }

    single<ProxyInstanceControlServiceImpl> {
        ProxyInstanceControlServiceImpl(
            proxyRepository = get<ProxyRepository>(),
        )
    }

    single<ProxyInstanceDataServiceImpl> {
        ProxyInstanceDataServiceImpl()
    }
}