package com.example.sioribi.ui

import androidx.datastore.preferences.core.mutablePreferencesOf
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WidgetUpdateWorkerTest {
    @Test
    fun `writeUiStateToPreferences stores state values`() {
        val preferences = mutablePreferencesOf()
        val state =
            YearProgressUiState(
                currentDay = 10,
                totalDays = 365,
                year = 2026,
                formatted = "10/365",
                progressPercent = 3,
            )

        writeUiStateToPreferences(preferences, state)

        assertThat(preferences[YearProgressWidget.KEY_CURRENT_DAY]).isEqualTo(10)
        assertThat(preferences[YearProgressWidget.KEY_TOTAL_DAYS]).isEqualTo(365)
        assertThat(preferences[YearProgressWidget.KEY_YEAR]).isEqualTo(2026)
        assertThat(preferences[YearProgressWidget.KEY_FORMATTED]).isEqualTo("10/365")
    }

    @Test
    fun `buildWidgetUpdateLogMessage formats state and reason`() {
        val state =
            YearProgressUiState(
                currentDay = 1,
                totalDays = 365,
                year = 2026,
                formatted = "1/365",
                progressPercent = 1,
            )

        val message = buildWidgetUpdateLogMessage(state, "Manual")

        assertThat(message).isEqualTo("Updated widget for 2026: 1/365 reason=Manual")
    }
}
