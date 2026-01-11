package com.example.sioribi.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.unit.ColorProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class YearProgressWidgetStateTest {
    @Test
    fun `normalizeWidgetValues applies defaults and sanitizes invalid values`() {
        val values =
            normalizeWidgetValues(
                WidgetRawValues(
                    currentDay = -5,
                    totalDays = 0,
                    year = null,
                    formatted = " ",
                ),
            )

        assertThat(values)
            .isEqualTo(
                WidgetValueState(
                    currentDay = 0,
                    totalDays = DEFAULT_TOTAL_DAYS,
                    year = 0,
                    formatted = DEFAULT_FORMATTED,
                ),
            )
    }

    @Test
    fun `normalizeWidgetValues preserves valid inputs`() {
        val values =
            normalizeWidgetValues(
                WidgetRawValues(
                    currentDay = 12,
                    totalDays = 366,
                    year = 2026,
                    formatted = "12/366",
                ),
            )

        assertThat(values)
            .isEqualTo(
                WidgetValueState(
                    currentDay = 12,
                    totalDays = 366,
                    year = 2026,
                    formatted = "12/366",
                ),
            )
    }

    @Test
    fun `shouldTriggerRefresh matches default state`() {
        assertThat(shouldTriggerRefresh(year = 0, formatted = DEFAULT_FORMATTED)).isTrue()
        assertThat(shouldTriggerRefresh(year = 2026, formatted = DEFAULT_FORMATTED)).isFalse()
        assertThat(shouldTriggerRefresh(year = 0, formatted = "1/365")).isFalse()
    }

    @Test
    fun `buildWidgetLayoutState combines values and resolved colors`() {
        val values =
            WidgetValueState(
                currentDay = 10,
                totalDays = 365,
                year = 2026,
                formatted = "10/365",
            )
        val gridLayout =
            GridLayout(
                columns = 2,
                rows = 2,
                dotSize = 1.dp,
                horizontalSpacing = 2.dp,
                verticalSpacing = 3.dp,
                padding = 4.dp,
            )
        val gridSize = DpSize(100.dp, 200.dp)
        val backgroundColorProvider = ColorProvider(Color(0xFF111111))
        val textColorProvider = ColorProvider(Color(0xFF222222))
        val activeColor = Color(0xFF333333)
        val inactiveColor = Color(0xFF444444)

        val state =
            buildWidgetLayoutState(
                values = values,
                inputs =
                    WidgetLayoutInputs(
                        gridLayout = gridLayout,
                        gridSize = gridSize,
                        backgroundColorProvider = backgroundColorProvider,
                        textColorProvider = textColorProvider,
                        activeColor = activeColor,
                        inactiveColor = inactiveColor,
                    ),
            )

        assertThat(state.gridLayout).isEqualTo(gridLayout)
        assertThat(state.gridSize).isEqualTo(gridSize)
        assertThat(state.backgroundColorProvider).isSameInstanceAs(backgroundColorProvider)
        assertThat(state.textColorProvider).isSameInstanceAs(textColorProvider)
        assertThat(state.year).isEqualTo(2026)
        assertThat(state.formatted).isEqualTo("10/365")
        assertThat(state.totalDays).isEqualTo(365)
        assertThat(state.currentDay).isEqualTo(10)
        assertThat(state.activeColor).isEqualTo(activeColor)
        assertThat(state.inactiveColor).isEqualTo(inactiveColor)
    }
}
