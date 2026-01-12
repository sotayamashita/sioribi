package com.example.sioribi.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class DefaultYearProgressRepositoryTest {
    @Test
    fun `non-leap year typical date computes expected progress`() {
        val repository =
            DefaultYearProgressRepository(FakeTimeDataSource(LocalDate.of(2026, 1, 4)))

        val result = repository.getYearProgress()

        assertThat(result.year).isEqualTo(2026)
        assertThat(result.totalDays).isEqualTo(365)
        assertThat(result.currentDay).isEqualTo(4)
    }

    @Test
    fun `leap day computes leap year totals`() {
        val repository =
            DefaultYearProgressRepository(FakeTimeDataSource(LocalDate.of(2024, 2, 29)))

        val result = repository.getYearProgress()

        assertThat(result.year).isEqualTo(2024)
        assertThat(result.totalDays).isEqualTo(366)
        assertThat(result.currentDay).isEqualTo(60)
    }

    @Test
    fun `leap year first day uses leap year total`() {
        val repository =
            DefaultYearProgressRepository(FakeTimeDataSource(LocalDate.of(2024, 1, 1)))

        val result = repository.getYearProgress()

        assertThat(result.totalDays).isEqualTo(366)
        assertThat(result.currentDay).isEqualTo(1)
    }

    @Test
    fun `day before leap day rounds consistently`() {
        val repository =
            DefaultYearProgressRepository(FakeTimeDataSource(LocalDate.of(2024, 2, 28)))

        val result = repository.getYearProgress()

        assertThat(result.totalDays).isEqualTo(366)
        assertThat(result.currentDay).isEqualTo(59)
    }

    @Test
    fun `first day of year rounds down percentage`() {
        val repository =
            DefaultYearProgressRepository(FakeTimeDataSource(LocalDate.of(2026, 1, 1)))

        val result = repository.getYearProgress()

        assertThat(result.totalDays).isEqualTo(365)
        assertThat(result.currentDay).isEqualTo(1)
    }

    @Test
    fun `last day of year reaches 100 percent`() {
        val repository =
            DefaultYearProgressRepository(FakeTimeDataSource(LocalDate.of(2026, 12, 31)))

        val result = repository.getYearProgress()

        assertThat(result.totalDays).isEqualTo(365)
        assertThat(result.currentDay).isEqualTo(365)
    }

    @Test
    fun `leap year last day reaches 100 percent`() {
        val repository =
            DefaultYearProgressRepository(FakeTimeDataSource(LocalDate.of(2024, 12, 31)))

        val result = repository.getYearProgress()

        assertThat(result.totalDays).isEqualTo(366)
        assertThat(result.currentDay).isEqualTo(366)
    }

    @Test
    fun `rounding uses nearest integer for mid-year`() {
        val repository =
            DefaultYearProgressRepository(FakeTimeDataSource(LocalDate.of(2026, 2, 1)))

        val result = repository.getYearProgress()

        assertThat(result.totalDays).isEqualTo(365)
        assertThat(result.currentDay).isEqualTo(32)
    }

    private class FakeTimeDataSource(
        private val date: LocalDate,
    ) : TimeDataSource {
        override fun today(): LocalDate = date
    }
}
