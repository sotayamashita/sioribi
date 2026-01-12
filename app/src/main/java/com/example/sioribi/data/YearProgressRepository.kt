package com.example.sioribi.data

import com.example.sioribi.domain.YearProgressModel

interface YearProgressRepository {
    fun getYearProgress(): YearProgressModel
}
