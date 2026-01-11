package com.example.sioribi.ui

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.MutablePreferences
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sioribi.SioribiApplication
import com.example.sioribi.domain.YearProgressModel

class WidgetUpdateWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val appGraph = (applicationContext as SioribiApplication).appGraph
        val model = appGraph.getYearProgressUseCase.execute()
        val manager = GlanceAppWidgetManager(applicationContext)
        val glanceIds = manager.getGlanceIds(YearProgressWidget::class.java)

        glanceIds.forEach { glanceId ->
            updateAppWidgetState(applicationContext, glanceId) { prefs ->
                writeModelToPreferences(prefs, model)
            }
        }

        YearProgressWidget().updateAll(applicationContext)

        val reason = inputData.getString(KEY_REFRESH_REASON) ?: "Unknown"
        Log.d(
            "WidgetUpdateWorker",
            "Updated widget for ${model.year}: ${model.formattedString} reason=$reason",
        )
        return Result.success()
    }
}

internal fun writeModelToPreferences(
    preferences: MutablePreferences,
    model: YearProgressModel,
) {
    preferences[YearProgressWidget.KEY_CURRENT_DAY] = model.currentDay
    preferences[YearProgressWidget.KEY_TOTAL_DAYS] = model.totalDays
    preferences[YearProgressWidget.KEY_YEAR] = model.year
    preferences[YearProgressWidget.KEY_FORMATTED] = model.formattedString
}
