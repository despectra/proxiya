package ru.despectra.proxiya.controller.config.proxies.v2

import ru.despectra.proxiya.controller.config.YamlConfigRepository
import java.nio.file.Paths
import java.util.TreeMap

class ProxiesListConfigRepository : YamlConfigRepository<ProxiesListConfig>(
    filePath = Paths.get("proxies-list.yaml"),
    serializer = ProxiesListConfig.serializer(),
    defaultConfig = ProxiesListConfig(TreeMap())
)
