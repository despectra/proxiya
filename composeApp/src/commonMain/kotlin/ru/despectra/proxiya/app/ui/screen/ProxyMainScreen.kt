package ru.despectra.proxiya.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Play
import compose.icons.feathericons.Power
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.despectra.proxiya.app.data.settings.ThemeMode
import ru.despectra.proxiya.app.ui.theme.ProxiyaTheme
import ru.despectra.proxiya.app.ui.theme.SpecialColors
import ru.despectra.proxiya.app.ui.viewmodel.ProxyItemVo
import ru.despectra.proxiya.app.ui.viewmodel.ProxyMainViewModel
import ru.despectra.proxiya.app.ui.viewmodel.ProxyState

@Composable
fun ProxyMainScreen(modifier: Modifier = Modifier, proxyId: String) {
    val viewModel: ProxyMainViewModel = koinViewModel(parameters = { parametersOf(proxyId) })
    val proxyState by viewModel.proxyState.collectAsState()
    val isStarting by viewModel.isStarting.collectAsState()

    // Отслеживаем изменения proxyId и обновляем ViewModel
    LaunchedEffect(proxyId) {
        viewModel.updateProxyId(proxyId)
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        when (val state = proxyState) {
            is ProxyState.Loading -> {
                LoadingContent()
            }
            is ProxyState.Loaded -> {
                Header(
                    proxyName = state.name,
                    proxyType = state.type,
                    proxyPort = state.port,
                    proxyStatus = state.status,
                    proxyProcessId = state.processId,
                    isStarting = isStarting,
                    onStartClick = viewModel::startProxy,
                    onStopClick = viewModel::stopProxy
                )
                Content()
            }
            is ProxyState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetry = viewModel::refresh
                )
            }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    modifier: Modifier = Modifier,
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Ошибка загрузки прокси",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Повторить")
        }
    }
}

@Composable
private fun Header(
    modifier: Modifier = Modifier,
    proxyName: String,
    proxyType: String,
    proxyPort: String,
    proxyStatus: ProxyItemVo.Status,
    proxyProcessId: String?,
    isStarting: Boolean,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                text = proxyName,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$proxyType on port $proxyPort",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                ProxyStatusBadge(
                    proxyStatus = proxyStatus,
                    proxyProcessId = proxyProcessId,
                )
            }
        }

        when (proxyStatus) {
            ProxyItemVo.Status.ACTIVE -> {
                OutlinedButton(
                    shape = RoundedCornerShape(12.dp),
                    onClick = onStopClick,
                ) {
                    Icon(
                        imageVector = FeatherIcons.Power,
                        contentDescription = "Stop proxy",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        text = "Stop",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            else -> {
                OutlinedButton(
                    shape = RoundedCornerShape(12.dp),
                    onClick = onStartClick,
                    enabled = !isStarting
                ) {
                    if (isStarting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = FeatherIcons.Play,
                            contentDescription = "Start proxy",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        text = if (isStarting) "Starting..." else "Start",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProxyStatusBadge(
    modifier: Modifier = Modifier,
    proxyStatus: ProxyItemVo.Status,
    proxyProcessId: String? = null,
) {
    val isInactive = proxyStatus != ProxyItemVo.Status.ACTIVE
    
    Box(
        modifier = modifier
            .wrapContentWidth()
            .height(24.dp)
            .run {
                if (isInactive) {
                    border(
                        width = 1.dp,
                        color = SpecialColors.proxyStatusInactive,
                        shape = RoundedCornerShape(12.dp),
                    )
                } else {
                    clip(RoundedCornerShape(12.dp))
                }
            }
            .background(
                color = when (proxyStatus) {
                    ProxyItemVo.Status.ACTIVE -> SpecialColors.proxyStatusRunning
                    ProxyItemVo.Status.ERROR -> SpecialColors.proxyStatusError
                    ProxyItemVo.Status.TRANSIENT -> SpecialColors.info
                    else -> Color.Transparent
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 8.dp),
            text = when {
                proxyStatus == ProxyItemVo.Status.ACTIVE && proxyProcessId != null -> "PID: $proxyProcessId"
                proxyStatus == ProxyItemVo.Status.TRANSIENT -> "Starting..."
                else -> "Inactive"
            },
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isInactive) .75f else 1f),
            maxLines = 1,
        )
    }
}

@Composable
private fun Content(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceDim)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

    }
}

@Preview
@Composable
fun ProxyMainScreenPreview() {
    ProxiyaTheme(themeMode = ThemeMode.DARK) {
        ProxyMainScreen(proxyId = "1")
    }
}