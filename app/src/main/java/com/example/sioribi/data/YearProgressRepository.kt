package com.example.sioribi.data

import com.example.sioribi.domain.YearProgress

interface YearProgressRepository {
    fun getYearProgress(): YearProgress
}
