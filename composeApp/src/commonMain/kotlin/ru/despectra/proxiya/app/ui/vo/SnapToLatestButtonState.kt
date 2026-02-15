package ru.despectra.proxiya.app.ui.vo

import androidx.compose.runtime.Stable

@Stable
sealed interface SnapToLatestButtonState {
    data object Unsnapped : SnapToLatestButtonState
    data class Snapped(val locked: Boolean = false) : SnapToLatestButtonState
}