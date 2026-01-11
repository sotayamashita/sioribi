package com.example.sioribi.ui

import androidx.datastore.preferences.core.mutablePreferencesOf
import com.example.sioribi.domain.YearProgressModel
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WidgetUpdateWorkerTest {
    @Test
    fun `writeModelToPreferences stores model values`() {
        val preferences = mutablePreferencesOf()
        val model =
            YearProgressModel(
                currentDay = 10,
                totalDays = 365,
                year = 2026,
                progressPercentage = 3,
                formattedString = "10/365",
            )

        writeModelToPreferences(preferences, model)

        assertThat(preferences[YearProgressWidget.KEY_CURRENT_DAY]).isEqualTo(10)
        assertThat(preferences[YearProgressWidget.KEY_TOTAL_DAYS]).isEqualTo(365)
        assertThat(preferences[YearProgressWidget.KEY_YEAR]).isEqualTo(2026)
        assertThat(preferences[YearProgressWidget.KEY_FORMATTED]).isEqualTo("10/365")
    }
}
