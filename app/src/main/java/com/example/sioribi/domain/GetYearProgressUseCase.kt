package com.example.sioribi.domain

import com.example.sioribi.data.TimeDataSource
import kotlin.math.roundToInt

class GetYearProgressUseCase(
    private val timeDataSource: TimeDataSource,
) {
    fun execute(): YearProgressModel {
        val today = timeDataSource.today()
        val year = today.year
        val totalDays = if (today.isLeapYear) 366 else 365
        val currentDay = today.dayOfYear
        val progressPercentage = (currentDay.toDouble() / totalDays.toDouble() * 100).roundToInt()
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
