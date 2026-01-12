package com.example.sioribi.ui

import androidx.datastore.preferences.core.MutablePreferences

internal fun writeUiStateToPreferences(
    preferences: MutablePreferences,
    state: YearProgressUiState,
) {
    preferences[YearProgressWidget.KEY_CURRENT_DAY] = state.currentDay
    preferences[YearProgressWidget.KEY_TOTAL_DAYS] = state.totalDays
    preferences[YearProgressWidget.KEY_YEAR] = state.year
    preferences[YearProgressWidget.KEY_FORMATTED] = state.formatted
}

internal fun buildWidgetUpdateLogMessage(
    state: YearProgressUiState,
    reason: String,
): String = "Updated widget for ${state.year}: ${state.formatted} reason=$reason"
