package ru.despectra.proxiya.controller

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.module
import ru.despectra.proxiya.controller.config.general.GeneralConfigRepository
import ru.despectra.proxiya.controller.config.proxies.ProxyConfigRepository
import ru.despectra.proxiya.controller.config.proxies.RegisteredProxiesConfigRepository
import java.nio.file.Path

fun startProxiyaKoin(): KoinApplication {
    return startKoin {

        // YAML Configs
        module {
            factory<GeneralConfigRepository> {
                GeneralConfigRepository()
            }

            factory<RegisteredProxiesConfigRepository> {
                RegisteredProxiesConfigRepository()
            }

            factory<ProxyConfigRepository> { (filePath: Path) ->
                ProxyConfigRepository(filePath)
            }
        }


    }
}