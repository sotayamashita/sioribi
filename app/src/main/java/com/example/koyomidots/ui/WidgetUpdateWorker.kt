package com.example.koyomidots.ui

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.PreferencesGlanceStateDefinition
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.datastore.preferences.core.toMutablePreferences
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
            updateAppWidgetState(
                context = applicationContext,
                definition = PreferencesGlanceStateDefinition,
                glanceId = glanceId
            ) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[YearProgressWidget.KEY_CURRENT_DAY] = model.currentDay
                    this[YearProgressWidget.KEY_TOTAL_DAYS] = model.totalDays
                    this[YearProgressWidget.KEY_YEAR] = model.year
                    this[YearProgressWidget.KEY_FORMATTED] = model.formattedString
                }
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
