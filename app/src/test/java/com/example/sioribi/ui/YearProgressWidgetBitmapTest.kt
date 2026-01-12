package com.example.sioribi.ui

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class YearProgressWidgetBitmapTest {
    @Test
    fun `computeBitmapSize scales by density and clamps to at least 1`() {
        val gridSize = DpSize(0.2.dp, 0.1.dp)

        val size = computeBitmapSize(gridSize, density = 2.0f)

        assertThat(size.width).isEqualTo(1)
        assertThat(size.height).isEqualTo(1)
    }

    @Test
    fun `computeBitmapSize rounds to nearest pixel`() {
        val gridSize = DpSize(10.4.dp, 20.6.dp)

        val size = computeBitmapSize(gridSize, density = 1.0f)

        assertThat(size.width).isEqualTo(10)
        assertThat(size.height).isEqualTo(21)
    }

    @Test
    fun `computeDotDrawSpecs scales sizes by density`() {
        val layout =
            GridLayout(
                columns = 7,
                rows = 10,
                dotSize = 2.dp,
                horizontalSpacing = 1.5.dp,
                verticalSpacing = 1.dp,
                padding = 0.dp,
            )

        val specs = computeDotDrawSpecs(layout, density = 2.0f)

        assertThat(specs.dotSizePx).isEqualTo(4.0f)
        assertThat(specs.drawRadiusPx).isEqualTo(0.9f)
        assertThat(specs.horizontalSpacingPx).isEqualTo(3.0f)
        assertThat(specs.verticalSpacingPx).isEqualTo(2.0f)
    }
}
