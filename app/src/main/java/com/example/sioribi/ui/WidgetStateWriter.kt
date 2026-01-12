package com.example.sioribi.ui

import com.example.sioribi.domain.YearProgressModel

interface WidgetStateWriter {
    suspend fun write(model: YearProgressModel)
}
