package com.example.sioribi.di

import android.content.Context
import com.example.sioribi.data.SystemTimeDataSource
import com.example.sioribi.data.TimeDataSource
import com.example.sioribi.domain.GetYearProgressUseCase
import com.example.sioribi.ui.WidgetRefreshCoordinator
import com.example.sioribi.ui.WorkManagerWidgetRefreshEnqueuer

class AppGraph(appContext: Context) {
    val timeDataSource: TimeDataSource = SystemTimeDataSource()
    val getYearProgressUseCase: GetYearProgressUseCase = GetYearProgressUseCase(timeDataSource)
    val widgetRefreshCoordinator: WidgetRefreshCoordinator =
        WidgetRefreshCoordinator(WorkManagerWidgetRefreshEnqueuer(appContext))
}
