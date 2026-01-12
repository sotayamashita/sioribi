package com.example.sioribi.ui

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import com.example.sioribi.domain.YearProgressModel

class GlanceWidgetStateWriter(
    private val context: Context,
    private val manager: GlanceAppWidgetManager = GlanceAppWidgetManager(context),
) : WidgetStateWriter {
    override suspend fun write(model: YearProgressModel) {
        val glanceIds = manager.getGlanceIds(YearProgressWidget::class.java)
        glanceIds.forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { prefs ->
                writeModelToPreferences(prefs, model)
            }
        }
    }
}

class GlanceWidgetRenderer(
    private val context: Context,
    private val widget: GlanceAppWidget = YearProgressWidget(),
) : WidgetRenderer {
    override suspend fun render() {
        widget.updateAll(context)
    }
}
