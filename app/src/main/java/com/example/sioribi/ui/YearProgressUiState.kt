package com.example.sioribi.ui

data class YearProgressUiState(
    val currentDay: Int,
    val totalDays: Int,
    val year: Int,
    val formatted: String,
    val progressPercent: Int,
)
