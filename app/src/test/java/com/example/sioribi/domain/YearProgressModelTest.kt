package com.example.sioribi.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class YearProgressModelTest {
    @Test
    fun `model preserves provided values even if out of range`() {
        val model =
            YearProgressModel(
                currentDay = -1,
                totalDays = 0,
                year = 2026,
                progressPercentage = 150,
                formattedString = "-1/0",
            )

        assertThat(model.currentDay).isEqualTo(-1)
        assertThat(model.totalDays).isEqualTo(0)
        assertThat(model.year).isEqualTo(2026)
        assertThat(model.progressPercentage).isEqualTo(150)
        assertThat(model.formattedString).isEqualTo("-1/0")
    }
}
