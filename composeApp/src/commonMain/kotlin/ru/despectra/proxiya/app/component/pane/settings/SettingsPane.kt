package ru.despectra.proxiya.app.component.pane.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.despectra.proxiya.app.component.pane.MaterialPane
import ru.despectra.proxiya.app.data.settings.AppSettings
import ru.despectra.proxiya.app.data.settings.ThemeMode
import ru.despectra.proxiya.app.ui.kit.settings.SettingsCard
import ru.despectra.proxiya.app.ui.kit.settings.SettingsSwitch

@Composable
fun SettingsPane(component: SettingsComponent) {
    val settings by component.appSettings.collectAsState()

    MaterialPane(isolated = false) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            AppearanceSettings(
                settings = settings,
                onThemeChange = component::updateTheme
            )

            ProxySettings(
                settings = settings,
                onAutoStartChange = component::updateAutoStartProxies
            )
        }
    }
}

@Composable
private fun AppearanceSettings(
    settings: AppSettings,
    onThemeChange: (ThemeMode) -> Unit
) {
    SettingsCard(
        title = "Appearance",
    ) {
        ThemeSelector(
            selectedTheme = settings.theme,
            onThemeSelected = onThemeChange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSelector(
    selectedTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.wrapContentWidth(),
        space = 8.dp,
    ) {
        ThemeMode.entries.forEachIndexed { index, theme ->
            SegmentedButton(
                selected = theme == selectedTheme,
                shape = SegmentedButtonDefaults.itemShape(index = index, count = ThemeMode.entries.size),
                onClick = { onThemeSelected(theme) },
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    text = theme.displayName,
                )
            }
        }
    }
}

@Composable
private fun ProxySettings(
    settings: AppSettings,
    onAutoStartChange: (Boolean) -> Unit
) {
    SettingsCard(
        title = "General proxy settings",
    ) {
        SettingsSwitch(
            label = "Autostart proxies on startup",
            checked = settings.autoStartProxies,
            onCheckedChange = onAutoStartChange
        )
    }
}
