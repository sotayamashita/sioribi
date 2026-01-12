package com.example.sioribi.data

import com.example.sioribi.domain.YearProgress

class DefaultYearProgressRepository(
    private val timeDataSource: TimeDataSource,
) : YearProgressRepository {
    companion object {
        private const val COMMON_YEAR_DAYS = 365
        private const val LEAP_YEAR_DAYS = 366
    }

    override fun getYearProgress(): YearProgress {
        val today = timeDataSource.today()
        val year = today.year
        val totalDays = if (today.isLeapYear) LEAP_YEAR_DAYS else COMMON_YEAR_DAYS
        val currentDay = today.dayOfYear
        return YearProgress(
            currentDay = currentDay,
            totalDays = totalDays,
            year = year,
        )
    }
}
