package com.example.sioribi.ui

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WidgetGridSizingTest {
    @Test
    fun computeGridLayout_fitsAllDaysInSmallSize() {
        val layout = computeGridLayout(
            totalDays = 365,
            size = DpSize(180.dp, 180.dp),
            footerHeight = 20.dp,
            footerSpacing = 12.dp,
            minColumns = 7,
            spacingRatio = 0.5f,
            paddingRatio = 2.0f
        )

        assertThat(layout.columns * layout.rows).isAtLeast(365)
        assertThat(layout.dotSize.value).isGreaterThan(0f)
    }

    @Test
    fun computeGridLayout_dotSizeGrowsWithMoreSpace() {
        val small = computeGridLayout(
            totalDays = 365,
            size = DpSize(180.dp, 180.dp),
            footerHeight = 20.dp,
            footerSpacing = 12.dp,
            minColumns = 7,
            spacingRatio = 0.5f,
            paddingRatio = 2.0f
        )
        val large = computeGridLayout(
            totalDays = 365,
            size = DpSize(378.dp, 489.dp),
            footerHeight = 20.dp,
            footerSpacing = 12.dp,
            minColumns = 7,
            spacingRatio = 0.5f,
            paddingRatio = 2.0f
        )

        assertThat(large.dotSize.value).isAtLeast(small.dotSize.value)
        assertThat(large.dotSize.value).isGreaterThan(0f)
    }

    @Test
    fun computeGridLayout_increasesColumnsForWideSize() {
        val layout = computeGridLayout(
            totalDays = 365,
            size = DpSize(600.dp, 240.dp),
            footerHeight = 20.dp,
            footerSpacing = 12.dp,
            minColumns = 7,
            spacingRatio = 0.55f,
            paddingRatio = 1.8f
        )

        assertThat(layout.columns).isGreaterThan(7)
    }

    @Test
    fun pickLargestSize_prefersMaxArea() {
        val sizes = listOf(
            DpSize(180.dp, 180.dp),
            DpSize(240.dp, 240.dp),
            DpSize(378.dp, 489.dp)
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
}
