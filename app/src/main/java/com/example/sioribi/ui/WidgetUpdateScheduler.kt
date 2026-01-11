package com.example.sioribi.ui

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.time.Clock
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object WidgetUpdateScheduler {
    private const val UNIQUE_DAILY_WORK = "year_progress_daily"
    private const val UPDATE_INTERVAL_HOURS = 24L

    fun scheduleDaily(context: Context) {
        val delay = computeInitialDelayMillis(Clock.systemDefaultZone())

        val request =
            PeriodicWorkRequestBuilder<WidgetUpdateWorker>(UPDATE_INTERVAL_HOURS, TimeUnit.HOURS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(workDataOf(KEY_REFRESH_REASON to RefreshReason.Periodic.name))
                .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_DAILY_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }
}

internal fun computeInitialDelayMillis(clock: Clock): Long {
    val now = ZonedDateTime.now(clock)
    val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(now.zone)
    return Duration.between(now, nextMidnight).toMillis().coerceAtLeast(0)
}

class ManualRefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        WidgetRefreshCoordinatorProvider.from(context).requestRefresh(RefreshReason.Manual)
    }
}
