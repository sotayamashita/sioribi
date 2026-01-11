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
    fun computeGridLayout_usesFallbackWhenSizeIsZero() {
        val layout =
            computeGridLayout(
                configForSize(
                    size = DpSize(0.dp, 0.dp),
                    minColumns = 0,
                ),
            )

        assertThat(layout.columns).isEqualTo(1)
        assertThat(layout.rows).isAtLeast(1)
        assertThat(layout.dotSize).isEqualTo(1.dp)
        assertThat(layout.horizontalSpacing).isEqualTo(0.dp)
        assertThat(layout.verticalSpacing).isEqualTo(0.dp)
        assertThat(layout.padding).isEqualTo(0.dp)
    }

    @Test
    fun computeGridLayout_usesFallbackWhenSizeIsNegative() {
        val layout =
            computeGridLayout(
                configForSize(
                    size = DpSize((-10).dp, (-5).dp),
                    minColumns = 0,
                ),
            )

        assertThat(layout.columns).isEqualTo(1)
        assertThat(layout.rows).isAtLeast(1)
        assertThat(layout.dotSize).isEqualTo(1.dp)
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

    @Test
    fun pickLargestSize_returnsFallbackWhenNull() {
        val fallback = DpSize(42.dp, 24.dp)

        val resolved = pickLargestSize(null, fallback)

        assertThat(resolved).isEqualTo(fallback)
    }

    @Test
    fun pickLargestSize_ignoresInvalidSizes() {
        val fallback = DpSize(42.dp, 24.dp)
        val sizes =
            listOf(
                DpSize(0.dp, 240.dp),
                DpSize((-10).dp, 240.dp),
                DpSize(240.dp, 0.dp),
            )

        val resolved = pickLargestSize(sizes, fallback)

        assertThat(resolved).isEqualTo(fallback)
    }

    private fun configForSize(
        size: DpSize,
        totalDays: Int = TOTAL_DAYS,
        spacingRatio: Float = DEFAULT_SPACING_RATIO,
        paddingRatio: Float = DEFAULT_PADDING_RATIO,
        minColumns: Int = MIN_COLUMNS,
    ): GridLayoutConfig =
        GridLayoutConfig(
            totalDays = totalDays,
            size = size,
            footerHeight = FOOTER_HEIGHT,
            footerSpacing = FOOTER_SPACING,
            minColumns = minColumns,
            spacingRatio = spacingRatio,
            paddingRatio = paddingRatio,
        )
}
