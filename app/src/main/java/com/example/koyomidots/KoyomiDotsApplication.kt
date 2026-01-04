package com.example.koyomidots

import android.app.Application
import com.example.koyomidots.di.AppGraph
import com.example.koyomidots.ui.WidgetUpdateScheduler

class KoyomiDotsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppGraph.init()
        WidgetUpdateScheduler.scheduleDaily(this)
    }
}
