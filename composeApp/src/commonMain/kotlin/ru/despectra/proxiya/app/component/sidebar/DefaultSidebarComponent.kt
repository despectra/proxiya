package ru.despectra.proxiya.app.component.sidebar

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.despectra.proxiya.app.data.proxy.ProxiesRepository
import ru.despectra.proxiya.app.ui.vo.toViewObject

class DefaultSidebarComponent(
    val context: ComponentContext,
    proxiesRepository: ProxiesRepository,
) : SidebarComponent, ComponentContext by context {

    private val scope = coroutineScope()

    private val _selectedSidebarItem = MutableStateFlow<SidebarItem>(SidebarItem.CreateNewProxyItem)

    override val sidebarItems: Flow<List<SidebarItem>> = proxiesRepository
        .proxiesFlow()
        .map { it.toViewObject() }
        .map { proxies ->
            buildList {
                add(SidebarItem.CreateNewProxyItem)
                proxies
                    .map { SidebarItem.ProxyItem(it) }
                    .let(::addAll)

                add(SidebarItem.SpringSpacerItem)
                add(SidebarItem.SettingsItem)
            }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(scope, SharingStarted.Lazily, initialValue = emptyList())

    override val selectedSidebarItem: Flow<SidebarItem> = _selectedSidebarItem.asStateFlow()

    override fun onSelectItem(item: SidebarItem) {
        _selectedSidebarItem.value = item
    }

    class Factory(
        private val proxiesRepository: ProxiesRepository,
    ) : SidebarComponent.Factory {
        override fun create(componentContext: ComponentContext): SidebarComponent {
            return DefaultSidebarComponent(
                componentContext,
                proxiesRepository
            )
        }
    }
}