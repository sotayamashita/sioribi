package com.example.sioribi.ui

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
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
        val refreshReason = refreshReasonFromName(inputData.getString(KEY_REFRESH_REASON))
        appGraph.widgetUpdateCoordinator.update(refreshReason)
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

internal fun buildWidgetUpdateLogMessage(
    model: YearProgressModel,
    reason: String,
): String = "Updated widget for ${model.year}: ${model.formattedString} reason=$reason"
