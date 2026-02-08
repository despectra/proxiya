package ru.despectra.proxiya.controller.di

import org.koin.dsl.module
import org.koin.dsl.onClose
import ru.despectra.proxiya.controller.AppStartConfig
import ru.despectra.proxiya.controller.config.proxy.ProxiesListConfigRepository
import ru.despectra.proxiya.controller.config.proxy.ProxyConfigMapper
import ru.despectra.proxiya.controller.grpc.ProxyControlServiceImpl
import ru.despectra.proxiya.controller.grpc.ProxyDataServiceImpl
import ru.despectra.proxiya.controller.grpc.instance.ProxyInstanceControlServiceImpl
import ru.despectra.proxiya.controller.grpc.instance.ProxyInstanceDataServiceImpl
import ru.despectra.proxiya.controller.interception.InterceptedEventsConsumer
import ru.despectra.proxiya.controller.interception.InterceptedEventsProducer
import ru.despectra.proxiya.controller.interception.ProxyInterceptionGateway
import ru.despectra.proxiya.controller.repository.MitmproxyProcessManager
import ru.despectra.proxiya.controller.repository.ProxyParamsValidator
import ru.despectra.proxiya.controller.repository.ProxyRepository
import ru.despectra.proxiya.controller.server.ProxiyaControllerServer
import ru.despectra.proxiya.controller.store.ProxyStore

fun coreModule() = module {
    single<ProxiyaControllerServer> {
        ProxiyaControllerServer(
            proxyControlService = get<ProxyControlServiceImpl>(),
            proxyDataService = get<ProxyDataServiceImpl>(),
            proxyInstanceControlService = get<ProxyInstanceControlServiceImpl>(),
            proxyInstanceDataService = get<ProxyInstanceDataServiceImpl>(),
        )
    }

    single<ProxyRepository> {
        ProxyRepository(
            mitmproxyProcessManager = get<MitmproxyProcessManager>(),
            proxyStore = get<ProxyStore>(),
            proxyParamsValidator = get<ProxyParamsValidator>()
        )
    } onClose { it?.close() }

    factory<ProxyParamsValidator> {
        ProxyParamsValidator()
    }

    single<ProxyStore> {
        ProxyStore(
            proxiesListConfigRepository = get<ProxiesListConfigRepository>(),
            proxyConfigMapper = get<ProxyConfigMapper>()
        )
    } onClose { it?.close() }

    single<MitmproxyProcessManager> {
        MitmproxyProcessManager(
            appStartConfig = get<AppStartConfig>()
        )
    } onClose { it?.close() }

    single<ProxyInterceptionGateway> {
        ProxyInterceptionGateway(
            proxyStore = get<ProxyStore>()
        )
    } onClose { it?.close() }

    factory<InterceptedEventsProducer> {
        get<ProxyInterceptionGateway>()
    }

    factory<InterceptedEventsConsumer> {
        get<ProxyInterceptionGateway>()
    }
}