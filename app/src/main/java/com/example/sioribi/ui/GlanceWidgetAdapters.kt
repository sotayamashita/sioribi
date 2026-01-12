package com.example.sioribi.ui

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll

class GlanceWidgetStateWriter(
    private val context: Context,
    private val manager: GlanceAppWidgetManager = GlanceAppWidgetManager(context),
) : WidgetStateWriter {
    override suspend fun write(state: YearProgressUiState) {
        val glanceIds = manager.getGlanceIds(YearProgressWidget::class.java)
        glanceIds.forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { prefs ->
                writeUiStateToPreferences(prefs, state)
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
