package ru.despectra.proxiya.controller.config.proxies

import ru.despectra.proxiya.controller.config.YamlConfigRepository
import java.nio.file.Path

class ProxyConfigRepository(filePath: Path) : YamlConfigRepository<ProxyConfig>(
    filePath = filePath,
    serializer = ProxyConfig.serializer(),
    defaultConfig = null,
)