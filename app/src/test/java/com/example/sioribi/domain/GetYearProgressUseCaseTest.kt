package com.example.sioribi.domain

import com.example.sioribi.data.YearProgressRepository
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GetYearProgressUseCaseTest {
    @Test
    fun `delegates to repository`() {
        val expected =
            YearProgress(
                currentDay = 4,
                totalDays = 365,
                year = 2026,
            )
        val useCase = GetYearProgressUseCase(FakeYearProgressRepository(expected))

        val result = useCase()

        assertThat(result).isEqualTo(expected)
    }

    private class FakeYearProgressRepository(
        private val model: YearProgress,
    ) : YearProgressRepository {
        override fun getYearProgress(): YearProgress = model
    }
}
