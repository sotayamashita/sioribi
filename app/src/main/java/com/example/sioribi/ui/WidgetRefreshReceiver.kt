package com.example.sioribi.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class WidgetRefreshReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val reason = resolveRefreshReason(intent.action)

        if (reason != null) {
            WidgetRefreshCoordinatorProvider.from(context).requestRefresh(reason)
        }
    }
}

internal fun resolveRefreshReason(action: String?): RefreshReason? =
    when (action) {
        Intent.ACTION_DATE_CHANGED,
        Intent.ACTION_TIME_CHANGED,
        Intent.ACTION_TIMEZONE_CHANGED,
        -> RefreshReason.TimeChanged

        Intent.ACTION_BOOT_COMPLETED -> RefreshReason.Boot

        Intent.ACTION_MY_PACKAGE_REPLACED -> RefreshReason.PackageReplaced

        else -> null
    }
