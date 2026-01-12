package com.example.sioribi.ui

import com.example.sioribi.domain.YearProgress
import kotlin.math.roundToInt

class YearProgressUiStateMapper {
    fun map(progress: YearProgress): YearProgressUiState {
        val totalDays = progress.totalDays.coerceAtLeast(1)
        val currentDay = progress.currentDay.coerceIn(0, totalDays)
        val formatted = "$currentDay/$totalDays"
        val progressPercent =
            (currentDay.toDouble() / totalDays.toDouble() * PERCENT_SCALE).roundToInt()
        return YearProgressUiState(
            currentDay = currentDay,
            totalDays = totalDays,
            year = progress.year,
            formatted = formatted,
            progressPercent = progressPercent,
        )
    }

    private companion object {
        private const val PERCENT_SCALE = 100
    }
}
