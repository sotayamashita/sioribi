package com.example.koyomidots.di

import com.example.koyomidots.data.SystemTimeDataSource
import com.example.koyomidots.data.TimeDataSource
import com.example.koyomidots.domain.GetYearProgressUseCase

object AppGraph {
    lateinit var timeDataSource: TimeDataSource
        private set
    lateinit var getYearProgressUseCase: GetYearProgressUseCase
        private set

    fun init() {
        timeDataSource = SystemTimeDataSource()
        getYearProgressUseCase = GetYearProgressUseCase(timeDataSource)
    }
}
