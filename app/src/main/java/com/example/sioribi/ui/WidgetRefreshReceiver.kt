package com.example.sioribi.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class WidgetRefreshReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val reason =
            when (intent.action) {
                Intent.ACTION_DATE_CHANGED,
                Intent.ACTION_TIME_CHANGED,
                Intent.ACTION_TIMEZONE_CHANGED,
                -> RefreshReason.TimeChanged

                Intent.ACTION_BOOT_COMPLETED -> RefreshReason.Boot

                Intent.ACTION_MY_PACKAGE_REPLACED -> RefreshReason.PackageReplaced

                else -> null
            }

        if (reason != null) {
            WidgetRefreshCoordinatorProvider.from(context).requestRefresh(reason)
        }
    }
}
