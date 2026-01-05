package com.example.sioribi.ui

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.sioribi.SioribiApplication

internal const val KEY_REFRESH_REASON = "refresh_reason"
private const val UNIQUE_IMMEDIATE_WORK = "year_progress_refresh"

interface WidgetRefreshEnqueuer {
    fun enqueueImmediate(reason: RefreshReason)
}

class WorkManagerWidgetRefreshEnqueuer(private val context: Context) : WidgetRefreshEnqueuer {
    override fun enqueueImmediate(reason: RefreshReason) {
        val request = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
            .setInputData(workDataOf(KEY_REFRESH_REASON to reason.name))
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_IMMEDIATE_WORK,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}

class WidgetRefreshCoordinator(
    private val enqueuer: WidgetRefreshEnqueuer
) {
    fun requestRefresh(reason: RefreshReason) {
        enqueuer.enqueueImmediate(reason)
    }
}

internal object WidgetRefreshCoordinatorProvider {
    var provider: (Context) -> WidgetRefreshCoordinator = {
        (it.applicationContext as SioribiApplication).appGraph.widgetRefreshCoordinator
    }

    fun from(context: Context): WidgetRefreshCoordinator = provider(context)
}
