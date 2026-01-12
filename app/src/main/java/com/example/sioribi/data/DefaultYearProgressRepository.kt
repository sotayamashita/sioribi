package com.example.sioribi.data

import com.example.sioribi.domain.YearProgressModel
import kotlin.math.roundToInt

class DefaultYearProgressRepository(
    private val timeDataSource: TimeDataSource,
) : YearProgressRepository {
    companion object {
        private const val COMMON_YEAR_DAYS = 365
        private const val LEAP_YEAR_DAYS = 366
        private const val PERCENT_SCALE = 100
    }

    override fun getYearProgress(): YearProgressModel {
        val today = timeDataSource.today()
        val year = today.year
        val totalDays = if (today.isLeapYear) LEAP_YEAR_DAYS else COMMON_YEAR_DAYS
        val currentDay = today.dayOfYear
        val progressPercentage =
            (currentDay.toDouble() / totalDays.toDouble() * PERCENT_SCALE).roundToInt()
        val formattedString = "$currentDay/$totalDays"
        return YearProgressModel(
            currentDay = currentDay,
            totalDays = totalDays,
            year = year,
            progressPercentage = progressPercentage,
            formattedString = formattedString,
        )
    }
}
