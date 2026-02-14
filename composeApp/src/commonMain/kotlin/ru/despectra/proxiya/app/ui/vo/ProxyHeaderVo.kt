package ru.despectra.proxiya.app.ui.vo

data class ProxyHeaderVo(
    val name: String,
    val description: String?,
    val proxyParameters: String,
    val proxyStatus: ProxyStatus,
    val processId: String,
    val proxySwitchButton: ButtonVo,
    val streamingButton: ButtonVo,
    val filterButton: ButtonVo,
    val rewriteButton: ButtonVo,
    val snapToLatestButton: SnapToLatestButtonVo,
) {
    data class ButtonVo(
        val text: String,
        val isEnabled: Boolean,
        val icon: ButtonIcon,
    )
    enum class ProxyStatus {
        Idle,
        Starting,
        Running,
        Stopping,
        Error
    }

    enum class ButtonIcon {
        PowerIndicator,

        Play,
        Pause,
        Filter,

        Rewrite,
    }

    data class SnapToLatestButtonVo(
        val state: State,
    ) {
        enum class State {
            Snapped,
            LockedSnap,
            FreeScrolling,
        }
    }

    object Defaults {
        val proxySwitchButton = ButtonVo(
            text = "Start",
            isEnabled = false,
            icon = ButtonIcon.PowerIndicator,
        )

        val streamingButton = ButtonVo(
            text = "Streaming",
            isEnabled = false,
            icon = ButtonIcon.Play
        )

        val filterButton = ButtonVo(
            text = "Filter..",
            isEnabled = true,
            icon = ButtonIcon.Filter,
        )

        val rewriteButton = ButtonVo(
            text = "Rewrite..",
            isEnabled = true,
            icon = ButtonIcon.Rewrite
        )

        val snapToLatestButton = SnapToLatestButtonVo(
            state = SnapToLatestButtonVo.State.Snapped
        )
    }

    companion object {
        val init = ProxyHeaderVo(
            name = "Loading ...",
            description = "",
            proxyParameters = "",
            proxyStatus = ProxyStatus.Idle,
            processId = "",
            proxySwitchButton = Defaults.proxySwitchButton,
            streamingButton = Defaults.streamingButton,
            filterButton = Defaults.filterButton,
            rewriteButton = Defaults.rewriteButton,
            snapToLatestButton = Defaults.snapToLatestButton,
        )
    }
}