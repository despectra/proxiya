package ru.despectra.proxiya.controller.config.proxies

import org.slf4j.LoggerFactory
import ru.despectra.proxiya.controller.config.proxies.v2.EndpointConfig
import ru.despectra.proxiya.controller.config.proxies.v2.ProxyConfig
import ru.despectra.proxiya.controller.config.proxies.v2.ReverseProxyParamsConfig
import ru.despectra.proxiya.controller.config.proxies.v2.UpstreamProxyParamsConfig
import ru.despectra.proxiya.controller.repository.ProxyId
import ru.despectra.proxiya.controller.utils.asProtoTimestamp
import ru.despectra.proxiya.proto.common.endpoint
import ru.despectra.proxiya.proto.proxy.ProxyInstance
import ru.despectra.proxiya.proto.proxy.ProxyType
import ru.despectra.proxiya.proto.proxy.createdAtOrNull
import ru.despectra.proxiya.proto.proxy.paramsOrNull
import ru.despectra.proxiya.proto.proxy.proxyInstance
import ru.despectra.proxiya.proto.proxy.proxyParams
import ru.despectra.proxiya.proto.proxy.reverseProxyParams
import ru.despectra.proxiya.proto.proxy.reverseProxyParamsOrNull
import ru.despectra.proxiya.proto.proxy.upstreamProxyParams
import ru.despectra.proxiya.proto.proxy.upstreamProxyParamsOrNull
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class ProxyConfigMapper {
    private val logger = LoggerFactory.getLogger(ProxyConfigMapper::class.java)

    fun proxyInstanceFromConfig(
        proxyId: ProxyId,
        proxyConfig: ProxyConfig
    ): ProxyInstance = proxyInstance {
        this.proxyId = proxyId
        val proxyType = ProxyType.entries.find { it.name == proxyConfig.type } ?: let {
            logger.warn("For proxy $proxyId unknown proxy type: ${proxyConfig.type} in config")
            ProxyType.PROXY_TYPE_UNSPECIFIED
        }
        this.params = proxyParams {
            this.name = proxyConfig.name
            this.type = proxyType
            this.port = proxyConfig.port
            when (proxyType) {
                ProxyType.PROXY_TYPE_UPSTREAM -> proxyConfig.upstreamProxyParams?.let { params ->
                    this.upstreamProxyParams = upstreamProxyParams {
                        destination = endpoint {
                            host = params.destination.host
                            port = params.destination.port
                        }
                    }
                }

                ProxyType.PROXY_TYPE_REVERSE -> proxyConfig.reverseProxyParams?.let { params ->
                    this.reverseProxyParams = reverseProxyParams {
                        params.destinations.map { destination ->
                            endpoint {
                                host = destination.host
                                port = destination.port
                            }
                        }.let { destinations.addAll(it) }
                    }
                }

                else -> {}
            }
            this.hostsWhitelist.addAll(proxyConfig.hostsWhitelist)
            this.hostsBlacklist.addAll(proxyConfig.hostsBlacklist)
            this.extraCmdParams.addAll(proxyConfig.extraCommandParams)
            this.autostart = proxyConfig.autostart
        }
        this.createdAt = proxyConfig.createdAt.toInstant(ZoneOffset.UTC).asProtoTimestamp()
        this.updatedAt = Instant.now().asProtoTimestamp()
    }

    fun proxyConfigFromInstance(proxyInstance: ProxyInstance): ProxyConfig? {
        val params = proxyInstance.paramsOrNull ?: return null
        val name = if (params.hasName()) params.name else return null
        val type = if (params.type != ProxyType.PROXY_TYPE_UNSPECIFIED) params.type else return null
        val port = if (params.hasPort()) params.port else return null
        val upstreamProxyParams = params.upstreamProxyParamsOrNull
        val reverseProxyParams = params.reverseProxyParamsOrNull
        val hostsWhitelist = params.hostsWhitelistList
        val hostsBlacklist = params.hostsBlacklistList
        val extraCommandParams = params.extraCmdParamsList
        val autostart = params.autostart
        val createdAt = proxyInstance.createdAtOrNull ?: return null

        return ProxyConfig(
            name = name,
            type = type.name,
            port = port,
            upstreamProxyParams = upstreamProxyParams?.let {
                UpstreamProxyParamsConfig(
                    destination = EndpointConfig(
                        host = it.destination.host,
                        port = it.destination.port
                    )
                )
            },
            reverseProxyParams = reverseProxyParams?.let {
                ReverseProxyParamsConfig(
                    destinations = it.destinationsList.map { destination ->
                        EndpointConfig(
                            host = destination.host,
                            port = destination.port
                        )
                    }
                )
            },
            hostsWhitelist = hostsWhitelist.toList(),
            hostsBlacklist = hostsBlacklist.toList(),
            extraCommandParams = extraCommandParams.toList(),
            autostart = autostart,
            createdAt = LocalDateTime.ofEpochSecond(
                createdAt.seconds,
                createdAt.nanos,
                ZoneOffset.UTC
            )
        )
    }
}