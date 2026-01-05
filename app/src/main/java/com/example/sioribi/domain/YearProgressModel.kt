package com.example.sioribi.domain

data class YearProgressModel(
    val currentDay: Int,
    val totalDays: Int,
    val year: Int,
    val progressPercentage: Int,
    val formattedString: String,
)
