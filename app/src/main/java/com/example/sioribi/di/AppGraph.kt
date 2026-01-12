package com.example.sioribi.di

import android.content.Context
import com.example.sioribi.data.DefaultYearProgressRepository
import com.example.sioribi.data.SystemTimeDataSource
import com.example.sioribi.data.TimeDataSource
import com.example.sioribi.data.YearProgressRepository
import com.example.sioribi.domain.GetYearProgressUseCase
import com.example.sioribi.ui.WidgetRefreshCoordinator
import com.example.sioribi.ui.WorkManagerWidgetRefreshEnqueuer

class AppGraph(
    appContext: Context,
) {
    val timeDataSource: TimeDataSource = SystemTimeDataSource()
    val yearProgressRepository: YearProgressRepository =
        DefaultYearProgressRepository(timeDataSource)
    val getYearProgressUseCase: GetYearProgressUseCase =
        GetYearProgressUseCase(yearProgressRepository)
    val widgetRefreshCoordinator: WidgetRefreshCoordinator =
        WidgetRefreshCoordinator(WorkManagerWidgetRefreshEnqueuer(appContext))
}
