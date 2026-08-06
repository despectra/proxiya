package ru.despectra.proxiya.app.ui.kit

import kotlin.test.Test
import kotlin.test.assertEquals

class TableColumnResizeTest {

    @Test
    fun `resizes adjacent columns using container weight scale`() {
        val resized = resizeAdjacentColumnWeights(
            firstWeight = 0.6f,
            secondWeight = 0.4f,
            deltaPx = 100f,
            containerWidthPx = 1_000f,
            totalWeight = 2f,
            minColumnWidthPx = 36f,
        )

        assertEquals(0.8f, resized.first, absoluteTolerance = TOLERANCE)
        assertEquals(0.2f, resized.second, absoluteTolerance = TOLERANCE)
        assertEquals(1f, resized.first + resized.second, absoluteTolerance = TOLERANCE)
    }

    @Test
    fun `clamps first column to minimum width`() {
        val resized = resizeAdjacentColumnWeights(
            firstWeight = 0.05f,
            secondWeight = 0.1f,
            deltaPx = -1_000f,
            containerWidthPx = 1_000f,
            totalWeight = 1f,
            minColumnWidthPx = 36f,
        )

        assertEquals(0.036f, resized.first, absoluteTolerance = TOLERANCE)
        assertEquals(0.114f, resized.second, absoluteTolerance = TOLERANCE)
    }

    @Test
    fun `clamps second column to minimum width`() {
        val resized = resizeAdjacentColumnWeights(
            firstWeight = 0.05f,
            secondWeight = 0.1f,
            deltaPx = 1_000f,
            containerWidthPx = 1_000f,
            totalWeight = 1f,
            minColumnWidthPx = 36f,
        )

        assertEquals(0.114f, resized.first, absoluteTolerance = TOLERANCE)
        assertEquals(0.036f, resized.second, absoluteTolerance = TOLERANCE)
    }

    @Test
    fun `uses safe equal split when pair cannot fit both minimum widths`() {
        val resized = resizeAdjacentColumnWeights(
            firstWeight = 0.05f,
            secondWeight = 0.05f,
            deltaPx = 100f,
            containerWidthPx = 600f,
            totalWeight = 1f,
            minColumnWidthPx = 36f,
        )

        assertEquals(0.05f, resized.first, absoluteTolerance = TOLERANCE)
        assertEquals(0.05f, resized.second, absoluteTolerance = TOLERANCE)
    }

    private companion object {
        const val TOLERANCE = 0.000_001f
    }
}
