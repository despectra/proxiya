package ru.despectra.proxiya.app.component.pane.proxy.create

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.despectra.proxiya.app.data.proxy.ProxyType
import ru.despectra.proxiya.app.ui.theme.ProxiyaCustomColors
import ru.despectra.proxiya.app.ui.vo.CreateProxyFormVo

@Composable
fun CreateProxyForm(component: CreateProxyComponent) {
    val formState by component.formState.collectAsState()
    val submitResultState by component.submitResultState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Create new proxy",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 32.dp),
        )

        ProxyTypeSelector(
            proxyType = formState.proxyType,
            onProxyTypeSelected = component::updateProxyType,
            modifier = Modifier.widthIn(min = 320.dp, max = 480.dp),
        )

        OutlinedTextField(
            value = formState.name,
            onValueChange = component::updateName,
            label = { Text("Name") },
            modifier = Modifier.widthIn(min = 480.dp, max = 720.dp).padding(top = 16.dp),
            singleLine = true,
        )

        OutlinedTextField(
            value = formState.port,
            onValueChange = component::updatePort,
            label = { Text("Port") },
            modifier = Modifier.widthIn(min = 480.dp, max = 720.dp).padding(top = 16.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            value = formState.additionalParams,
            onValueChange = component::updateAdditionalParams,
            label = { Text("Additional params (mitmproxy)") },
            modifier = Modifier.widthIn(min = 480.dp, max = 720.dp).padding(top = 16.dp),
        )

        Button(
            onClick = component::submit,
            modifier = Modifier.widthIn(min = 120.dp).padding(top = 16.dp),
            enabled = submitResultState != CreateProxyFormVo.SubmitResult.InProgress,
        ) {
            Text("Create")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (val result = submitResultState) {
            CreateProxyFormVo.SubmitResult.Success -> {
                Text(
                    text = "Proxy created successfully",
                    style = MaterialTheme.typography.bodySmall,
                    color = ProxiyaCustomColors.success,
                )
            }
            is CreateProxyFormVo.SubmitResult.Error -> {
                Text(
                    text = result.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            else -> {
                // Nothing
            }
        }
    }
}

@Composable
fun ProxyTypeSelector(
    proxyType: ProxyType,
    onProxyTypeSelected: (ProxyType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.padding(end = 12.dp),
            text = "Proxy type",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth(),
        ) {
            ProxyType.entries.forEachIndexed { index, item ->
                SegmentedButton(
                    modifier = Modifier.widthIn(min = 120.dp),
                    selected = proxyType == item,
                    onClick = {
                        onProxyTypeSelected(item)
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = ProxyType.entries.size),
                ) {
                    Text(
                        text = item.label(),
                    )
                }
            }
        }
    }
}

private fun ProxyType.label() = when (this) {
    ProxyType.Https -> "HTTPS"
    ProxyType.Socks5 -> "SOCKS 5"
}