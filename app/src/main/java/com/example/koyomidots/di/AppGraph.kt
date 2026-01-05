package com.example.koyomidots.di

import android.content.Context
import com.example.koyomidots.data.SystemTimeDataSource
import com.example.koyomidots.data.TimeDataSource
import com.example.koyomidots.domain.GetYearProgressUseCase
import com.example.koyomidots.ui.WidgetRefreshCoordinator
import com.example.koyomidots.ui.WorkManagerWidgetRefreshEnqueuer

class AppGraph(appContext: Context) {
    val timeDataSource: TimeDataSource = SystemTimeDataSource()
    val getYearProgressUseCase: GetYearProgressUseCase = GetYearProgressUseCase(timeDataSource)
    val widgetRefreshCoordinator: WidgetRefreshCoordinator =
        WidgetRefreshCoordinator(WorkManagerWidgetRefreshEnqueuer(appContext))
}
