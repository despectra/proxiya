package ru.despectra.proxiya.controller.config

import org.junit.jupiter.api.Test
import ru.despectra.proxiya.controller.config.general.GeneralConfig
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ConfigManagerTest {
    
    @Test
    fun `should create default config`() {
        val config = GeneralConfig.default()
        
        assertNotNull(config)
        assertEquals(false, config.autoStartProxies)
    }
    
    @Test
    fun `should create config with custom values`() {
        val config = GeneralConfig(autoStartProxies = true)
        
        assertNotNull(config)
        assertEquals(true, config.autoStartProxies)
    }
}
