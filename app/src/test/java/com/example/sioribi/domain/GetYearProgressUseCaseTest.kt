package com.example.sioribi.domain

import com.example.sioribi.data.YearProgressRepository
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GetYearProgressUseCaseTest {
    @Test
    fun `delegates to repository`() {
        val expected =
            YearProgressModel(
                currentDay = 4,
                totalDays = 365,
                year = 2026,
                progressPercentage = 1,
                formattedString = "4/365",
            )
        val useCase = GetYearProgressUseCase(FakeYearProgressRepository(expected))

        val result = useCase.execute()

        assertThat(result).isEqualTo(expected)
    }

    private class FakeYearProgressRepository(
        private val model: YearProgressModel,
    ) : YearProgressRepository {
        override fun getYearProgress(): YearProgressModel = model
    }
}
