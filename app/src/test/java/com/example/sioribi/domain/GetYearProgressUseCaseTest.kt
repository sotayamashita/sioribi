package com.example.sioribi.domain

import com.example.sioribi.data.TimeDataSource
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class GetYearProgressUseCaseTest {
    @Test
    fun `non-leap year typical date computes expected progress`() {
        // Technique: Equivalence Partitioning (typical non-leap year date).
        val useCase = GetYearProgressUseCase(FakeTimeDataSource(LocalDate.of(2026, 1, 4)))

        val result = useCase.execute()

        assertThat(result.year).isEqualTo(2026)
        assertThat(result.totalDays).isEqualTo(365)
        assertThat(result.currentDay).isEqualTo(4)
        assertThat(result.progressPercentage).isEqualTo(1)
        assertThat(result.formattedString).isEqualTo("4/365")
    }

    @Test
    fun `leap day computes leap year totals`() {
        // Technique: Boundary Value Analysis (leap day boundary).
        val useCase = GetYearProgressUseCase(FakeTimeDataSource(LocalDate.of(2024, 2, 29)))

        val result = useCase.execute()

        assertThat(result.year).isEqualTo(2024)
        assertThat(result.totalDays).isEqualTo(366)
        assertThat(result.currentDay).isEqualTo(60)
        assertThat(result.progressPercentage).isEqualTo(16)
        assertThat(result.formattedString).isEqualTo("60/366")
    }

    @Test
    fun `leap year first day uses leap year total`() {
        // Technique: Boundary Value Analysis (start of leap year).
        val useCase = GetYearProgressUseCase(FakeTimeDataSource(LocalDate.of(2024, 1, 1)))

        val result = useCase.execute()

        assertThat(result.totalDays).isEqualTo(366)
        assertThat(result.currentDay).isEqualTo(1)
        assertThat(result.progressPercentage).isEqualTo(0)
        assertThat(result.formattedString).isEqualTo("1/366")
    }

    @Test
    fun `day before leap day rounds consistently`() {
        // Technique: Boundary Value Analysis (day before leap day).
        val useCase = GetYearProgressUseCase(FakeTimeDataSource(LocalDate.of(2024, 2, 28)))

        val result = useCase.execute()

        assertThat(result.totalDays).isEqualTo(366)
        assertThat(result.currentDay).isEqualTo(59)
        assertThat(result.progressPercentage).isEqualTo(16)
        assertThat(result.formattedString).isEqualTo("59/366")
    }

    @Test
    fun `first day of year rounds down percentage`() {
        // Technique: Boundary Value Analysis (start of year).
        val useCase = GetYearProgressUseCase(FakeTimeDataSource(LocalDate.of(2026, 1, 1)))

        val result = useCase.execute()

        assertThat(result.totalDays).isEqualTo(365)
        assertThat(result.currentDay).isEqualTo(1)
        assertThat(result.progressPercentage).isEqualTo(0)
        assertThat(result.formattedString).isEqualTo("1/365")
    }

    @Test
    fun `last day of year reaches 100 percent`() {
        // Technique: Boundary Value Analysis (end of year).
        val useCase = GetYearProgressUseCase(FakeTimeDataSource(LocalDate.of(2026, 12, 31)))

        val result = useCase.execute()

        assertThat(result.totalDays).isEqualTo(365)
        assertThat(result.currentDay).isEqualTo(365)
        assertThat(result.progressPercentage).isEqualTo(100)
        assertThat(result.formattedString).isEqualTo("365/365")
    }

    @Test
    fun `leap year last day reaches 100 percent`() {
        // Technique: Boundary Value Analysis (end of leap year).
        val useCase = GetYearProgressUseCase(FakeTimeDataSource(LocalDate.of(2024, 12, 31)))

        val result = useCase.execute()

        assertThat(result.totalDays).isEqualTo(366)
        assertThat(result.currentDay).isEqualTo(366)
        assertThat(result.progressPercentage).isEqualTo(100)
        assertThat(result.formattedString).isEqualTo("366/366")
    }

    @Test
    fun `rounding uses nearest integer for mid-year`() {
        // Technique: Boundary Value Analysis (rounding behavior).
        val useCase = GetYearProgressUseCase(FakeTimeDataSource(LocalDate.of(2026, 2, 1)))

        val result = useCase.execute()

        assertThat(result.totalDays).isEqualTo(365)
        assertThat(result.currentDay).isEqualTo(32)
        assertThat(result.progressPercentage).isEqualTo(9)
        assertThat(result.formattedString).isEqualTo("32/365")
    }

    private class FakeTimeDataSource(
        private val date: LocalDate,
    ) : TimeDataSource {
        override fun today(): LocalDate = date
    }
}
