package com.example.sioribi.ui

interface WidgetStateWriter {
    suspend fun write(state: YearProgressUiState)
}
