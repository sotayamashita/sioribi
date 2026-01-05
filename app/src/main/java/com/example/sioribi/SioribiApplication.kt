package com.example.sioribi

import android.app.Application
import com.example.sioribi.di.AppGraph
import com.example.sioribi.ui.WidgetUpdateScheduler

class SioribiApplication : Application() {
    val appGraph: AppGraph by lazy { AppGraph(this) }

    override fun onCreate() {
        super.onCreate()
        WidgetUpdateScheduler.scheduleDaily(this)
    }
}
