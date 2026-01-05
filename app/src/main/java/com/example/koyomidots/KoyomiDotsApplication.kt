package com.example.koyomidots

import android.app.Application
import com.example.koyomidots.di.AppGraph
import com.example.koyomidots.ui.WidgetUpdateScheduler

class KoyomiDotsApplication : Application() {
    val appGraph: AppGraph by lazy { AppGraph(this) }

    override fun onCreate() {
        super.onCreate()
        WidgetUpdateScheduler.scheduleDaily(this)
    }
}
