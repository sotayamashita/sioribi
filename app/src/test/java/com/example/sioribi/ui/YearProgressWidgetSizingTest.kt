package com.example.sioribi.ui

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class YearProgressWidgetSizingTest {
    @Test
    fun `resolveEffectiveSize prefers non-zero local size`() {
        val localSize = DpSize(120.dp, 240.dp)
        val widgetSize = DpSize(80.dp, 160.dp)

        val resolved = resolveEffectiveSize(localSize, widgetSize)

        assertThat(resolved).isEqualTo(localSize)
    }

    @Test
    fun `resolveEffectiveSize falls back when local size is zero`() {
        val localSize = DpSize(0.dp, 0.dp)
        val widgetSize = DpSize(80.dp, 160.dp)

        val resolved = resolveEffectiveSize(localSize, widgetSize)

        assertThat(resolved).isEqualTo(widgetSize)
    }

    @Test
    fun `buildGridLayout enforces minimum columns`() {
        val layout = buildGridLayout(totalDays = 3, size = DpSize(100.dp, 100.dp))

        assertThat(layout.columns).isAtLeast(7)
        assertThat(layout.rows).isAtLeast(1)
    }

    @Test
    fun `buildGridSize accounts for padding and footer`() {
        val gridLayout =
            GridLayout(
                columns = 7,
                rows = 10,
                dotSize = 5.dp,
                horizontalSpacing = 1.dp,
                verticalSpacing = 1.dp,
                padding = 8.dp,
            )
        val effectiveSize = DpSize(200.dp, 300.dp)

        val gridSize = buildGridSize(effectiveSize, gridLayout)

        assertThat(gridSize.width).isEqualTo(184.dp)
        assertThat(gridSize.height).isEqualTo(252.dp)
    }
}
