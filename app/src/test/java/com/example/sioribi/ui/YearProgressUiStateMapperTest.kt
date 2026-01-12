package com.example.sioribi.ui

import com.example.sioribi.domain.YearProgress
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class YearProgressUiStateMapperTest {
    private val mapper = YearProgressUiStateMapper()

    @Test
    fun `maps non-leap year to formatted and percent`() {
        val state =
            mapper.map(
                YearProgress(
                    currentDay = 4,
                    totalDays = 365,
                    year = 2026,
                ),
            )

        assertThat(state.formatted).isEqualTo("4/365")
        assertThat(state.progressPercent).isEqualTo(1)
    }

    @Test
    fun `maps leap day to formatted and percent`() {
        val state =
            mapper.map(
                YearProgress(
                    currentDay = 60,
                    totalDays = 366,
                    year = 2024,
                ),
            )

        assertThat(state.formatted).isEqualTo("60/366")
        assertThat(state.progressPercent).isEqualTo(16)
    }

    @Test
    fun `maps year boundaries to expected percent`() {
        val first =
            mapper.map(
                YearProgress(
                    currentDay = 1,
                    totalDays = 365,
                    year = 2026,
                ),
            )
        val last =
            mapper.map(
                YearProgress(
                    currentDay = 365,
                    totalDays = 365,
                    year = 2026,
                ),
            )

        assertThat(first.progressPercent).isEqualTo(0)
        assertThat(last.progressPercent).isEqualTo(100)
    }

    @Test
    fun `clamps invalid day and total values`() {
        val state =
            mapper.map(
                YearProgress(
                    currentDay = -5,
                    totalDays = 0,
                    year = 2026,
                ),
            )

        assertThat(state.currentDay).isEqualTo(0)
        assertThat(state.totalDays).isEqualTo(1)
        assertThat(state.formatted).isEqualTo("0/1")
        assertThat(state.progressPercent).isEqualTo(0)
    }
}
