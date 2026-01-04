package com.example.koyomidots.ui

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.koyomidots.di.AppGraph

class WidgetUpdateWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val model = AppGraph.getYearProgressUseCase.execute()
        val manager = GlanceAppWidgetManager(applicationContext)
        val glanceIds = manager.getGlanceIds(YearProgressWidget::class.java)

        glanceIds.forEach { glanceId ->
            updateAppWidgetState(applicationContext, glanceId) { prefs ->
                prefs[YearProgressWidget.KEY_CURRENT_DAY] = model.currentDay
                prefs[YearProgressWidget.KEY_TOTAL_DAYS] = model.totalDays
                prefs[YearProgressWidget.KEY_YEAR] = model.year
                prefs[YearProgressWidget.KEY_FORMATTED] = model.formattedString
            }
        }

        YearProgressWidget().updateAll(applicationContext)

        Log.d(
            "WidgetUpdateWorker",
            "Updated widget for ${model.year}: ${model.formattedString}"
        )
        return Result.success()
    }
}
