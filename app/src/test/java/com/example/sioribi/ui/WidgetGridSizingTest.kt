package com.example.sioribi.ui

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WidgetGridSizingTest {
    private companion object {
        const val TOTAL_DAYS = 365
        val FOOTER_HEIGHT = 20.dp
        val FOOTER_SPACING = 12.dp
        const val MIN_COLUMNS = 7
        const val DEFAULT_SPACING_RATIO = 0.5f
        const val DEFAULT_PADDING_RATIO = 2.0f
        const val WIDE_SPACING_RATIO = 0.55f
        const val WIDE_PADDING_RATIO = 1.8f
    }

    @Test
    fun computeGridLayout_fitsAllDaysInSmallSize() {
        val layout =
            computeGridLayout(
                configForSize(DpSize(180.dp, 180.dp)),
            )

        assertThat(layout.columns * layout.rows).isAtLeast(TOTAL_DAYS)
        assertThat(layout.dotSize.value).isGreaterThan(0f)
    }

    @Test
    fun computeGridLayout_dotSizeGrowsWithMoreSpace() {
        val small =
            computeGridLayout(
                configForSize(DpSize(180.dp, 180.dp)),
            )
        val large =
            computeGridLayout(
                configForSize(DpSize(378.dp, 489.dp)),
            )

        assertThat(large.dotSize.value).isAtLeast(small.dotSize.value)
        assertThat(large.dotSize.value).isGreaterThan(0f)
    }

    @Test
    fun computeGridLayout_increasesColumnsForWideSize() {
        val layout =
            computeGridLayout(
                configForSize(
                    size = DpSize(600.dp, 240.dp),
                    spacingRatio = WIDE_SPACING_RATIO,
                    paddingRatio = WIDE_PADDING_RATIO,
                ),
            )

        assertThat(layout.columns).isGreaterThan(7)
    }

    @Test
    fun pickLargestSize_prefersMaxArea() {
        val sizes =
            listOf(
                DpSize(180.dp, 180.dp),
                DpSize(240.dp, 240.dp),
                DpSize(378.dp, 489.dp),
            )

        val resolved = pickLargestSize(sizes, DpSize(1.dp, 1.dp))

        assertThat(resolved).isEqualTo(DpSize(378.dp, 489.dp))
    }

    @Test
    fun pickLargestSize_fallsBackWhenEmpty() {
        val fallback = DpSize(181.dp, 203.dp)

        val resolved = pickLargestSize(emptyList(), fallback)

        assertThat(resolved).isEqualTo(fallback)
    }

    private fun configForSize(
        size: DpSize,
        spacingRatio: Float = DEFAULT_SPACING_RATIO,
        paddingRatio: Float = DEFAULT_PADDING_RATIO,
    ): GridLayoutConfig =
        GridLayoutConfig(
            totalDays = TOTAL_DAYS,
            size = size,
            footerHeight = FOOTER_HEIGHT,
            footerSpacing = FOOTER_SPACING,
            minColumns = MIN_COLUMNS,
            spacingRatio = spacingRatio,
            paddingRatio = paddingRatio,
        )
}
