package com.example.koyomidots.ui

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.state.PreferencesGlanceStateDefinition
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Arrangement
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.GridCells
import androidx.glance.layout.LazyVerticalGrid
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.background
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.items
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentHeight
import androidx.glance.layout.wrapContentWidth
import androidx.glance.text.FontFamily
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.unit.dp
import androidx.glance.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.compose.ui.graphics.Color

class YearProgressWidget : GlanceAppWidget() {
    companion object {
        val KEY_CURRENT_DAY = intPreferencesKey("current_day")
        val KEY_TOTAL_DAYS = intPreferencesKey("total_days")
        val KEY_YEAR = intPreferencesKey("year")
        val KEY_FORMATTED = stringPreferencesKey("formatted")

        private val ACTIVE_COLOR = ColorProvider(
            light = Color(0xFF2E2A24),
            dark = Color(0xFFF1E7D8)
        )
        private val INACTIVE_COLOR = ColorProvider(
            light = Color(0xFFD8CBB8),
            dark = Color(0xFF3B332B)
        )
        private val TEXT_COLOR = ColorProvider(
            light = Color(0xFF2E2A24),
            dark = Color(0xFFF1E7D8)
        )
        private const val COLUMNS = 14
    }

    override val stateDefinition = PreferencesGlanceStateDefinition

    @Composable
    override fun Content() {
        GlanceTheme {
            val prefs = currentState<Preferences>()
            val currentDay = prefs[KEY_CURRENT_DAY] ?: 0
            val totalDays = prefs[KEY_TOTAL_DAYS] ?: 365
            val year = prefs[KEY_YEAR] ?: 0
            val formatted = prefs[KEY_FORMATTED] ?: "0/0"

            Column(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp)
                    .clickable(actionRunCallback<ManualRefreshAction>()),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                LazyVerticalGrid(
                    gridCells = GridCells.Fixed(COLUMNS),
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                    modifier = GlanceModifier.wrapContentWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(totalDays) { index ->
                        val dayIndex = index + 1
                        val color = if (dayIndex <= currentDay) ACTIVE_COLOR else INACTIVE_COLOR
                        Box(
                            modifier = GlanceModifier
                                .size(4.dp)
                                .background(color)
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(12.dp))

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                ) {
                    Text(
                        text = if (year == 0) "----" else year.toString(),
                        style = TextStyle(
                            color = TEXT_COLOR,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(
                        text = formatted,
                        style = TextStyle(
                            color = TEXT_COLOR,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
        }
    }
}

class YearProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = YearProgressWidget()
}
