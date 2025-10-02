package ru.despectra.proxiya.controller.di

import org.koin.core.KoinApplication
import org.koin.dsl.module
import org.koin.dsl.onClose
import ru.despectra.proxiya.controller.config.proxies.ProxyConfigMapper
import ru.despectra.proxiya.controller.config.proxies.v2.ProxiesListConfigRepository
import ru.despectra.proxiya.controller.grpc.ProxyControlServiceImpl
import ru.despectra.proxiya.controller.grpc.ProxyDataServiceImpl
import ru.despectra.proxiya.controller.grpc.instance.ProxyInstanceControlServiceImpl
import ru.despectra.proxiya.controller.grpc.instance.ProxyInstanceDataServiceImpl
import ru.despectra.proxiya.controller.repository.MitmproxyProcessManager
import ru.despectra.proxiya.controller.repository.ProxyParamsValidator
import ru.despectra.proxiya.controller.repository.ProxyRepository
import ru.despectra.proxiya.controller.server.ProxiyaControllerServer
import ru.despectra.proxiya.controller.store.ProxyStore

fun KoinApplication.coreModule() = module {
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
        MitmproxyProcessManager()
    } onClose { it?.close() }
}