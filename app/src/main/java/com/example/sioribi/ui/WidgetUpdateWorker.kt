package com.example.sioribi.ui

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sioribi.SioribiApplication

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
