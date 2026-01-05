package com.example.koyomidots.ui

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.util.Log
import android.util.SizeF
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.background
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontFamily
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.layout.Column
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.toArgb
import com.example.koyomidots.R
import kotlin.math.roundToInt

class YearProgressWidget : GlanceAppWidget() {
    companion object {
        val KEY_CURRENT_DAY = intPreferencesKey("current_day")
        val KEY_TOTAL_DAYS = intPreferencesKey("total_days")
        val KEY_YEAR = intPreferencesKey("year")
        val KEY_FORMATTED = stringPreferencesKey("formatted")

        private val ACTIVE_COLOR_RES = R.color.widget_dot_active
        private val INACTIVE_COLOR_RES = R.color.widget_dot_inactive
        private val TEXT_COLOR_RES = R.color.widget_text
        private val FOOTER_SPACING = 12.dp
        private val FOOTER_HEIGHT = 20.dp
        private const val MIN_GRID_COLUMNS = 7
        private const val DOT_SPACING_RATIO = 0.55f
        private const val PADDING_RATIO = 1.8f
    }

    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val widgetSize = resolveWidgetSize(context, id)
        provideContent {
            WidgetContent(widgetSize)
        }
    }

    @Composable
    private fun WidgetContent(widgetSize: DpSize) {
        GlanceTheme {
            val context = LocalContext.current
            val prefs = currentState<Preferences>()
            val currentDay = prefs[KEY_CURRENT_DAY] ?: 0
            val totalDays = prefs[KEY_TOTAL_DAYS] ?: 365
            val year = prefs[KEY_YEAR] ?: 0
            val formatted = prefs[KEY_FORMATTED] ?: "0/0"
            val activeColor = Color(ContextCompat.getColor(context, ACTIVE_COLOR_RES))
            val inactiveColor = Color(ContextCompat.getColor(context, INACTIVE_COLOR_RES))
            val textColorProvider = ColorProvider(
                Color(ContextCompat.getColor(context, TEXT_COLOR_RES))
            )
            val backgroundColorProvider = ColorProvider(
                Color(ContextCompat.getColor(context, R.color.widget_background))
            )

            val localSize = LocalSize.current
            val effectiveSize = if (localSize.width.value > 0f && localSize.height.value > 0f) {
                localSize
            } else {
                widgetSize
            }
            val gridLayout = computeGridLayout(
                totalDays = totalDays,
                size = effectiveSize,
                footerHeight = FOOTER_HEIGHT,
                footerSpacing = FOOTER_SPACING,
                minColumns = MIN_GRID_COLUMNS,
                spacingRatio = DOT_SPACING_RATIO,
                paddingRatio = PADDING_RATIO
            )
            val gridSize = DpSize(
                width = (effectiveSize.width - (gridLayout.padding * 2)).coerceAtLeast(0.dp),
                height = (effectiveSize.height - (gridLayout.padding * 2) - FOOTER_HEIGHT - FOOTER_SPACING)
                    .coerceAtLeast(0.dp)
            )
            Log.d(
                "YearProgressWidget",
                "Widget size=${widgetSize.width.value}x${widgetSize.height.value} " +
                    "local=${localSize.width.value}x${localSize.height.value} " +
                    "effective=${effectiveSize.width.value}x${effectiveSize.height.value} " +
                    "grid=${gridSize.width.value}x${gridSize.height.value} " +
                    "grid=${gridLayout.columns}x${gridLayout.rows} " +
                    "dot=${gridLayout.dotSize.value} " +
                    "spacing=${gridLayout.horizontalSpacing.value}x${gridLayout.verticalSpacing.value} " +
                    "padding=${gridLayout.padding.value}"
            )

            Column(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(gridLayout.padding)
                    .background(backgroundColorProvider)
                    .clickable(actionRunCallback<ManualRefreshAction>()),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                val bitmap = remember(
                    totalDays,
                    currentDay,
                    gridSize,
                    gridLayout
                ) {
                    buildDotBitmap(
                        context = context,
                        gridSize = gridSize,
                        layout = gridLayout,
                        totalDays = totalDays,
                        currentDay = currentDay,
                        activeColor = activeColor,
                        inactiveColor = inactiveColor
                    )
                }
                Image(
                    provider = ImageProvider(bitmap = bitmap),
                    contentDescription = null,
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(gridSize.height),
                    contentScale = ContentScale.FillBounds
                )

                Spacer(modifier = GlanceModifier.height(FOOTER_SPACING))

                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.Horizontal.Start
                ) {
                    Text(
                        text = if (year == 0) "----" else year.toString(),
                        style = TextStyle(
                            color = textColorProvider,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Text(
                        text = formatted,
                        modifier = GlanceModifier.fillMaxWidth(),
                        style = TextStyle(
                            color = textColorProvider,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.End
                        )
                    )
                }
            }
        }
    }

    private fun buildDotBitmap(
        context: Context,
        gridSize: DpSize,
        layout: GridLayout,
        totalDays: Int,
        currentDay: Int,
        activeColor: Color,
        inactiveColor: Color
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val widthPx = (gridSize.width.value * density).roundToInt().coerceAtLeast(1)
        val heightPx = (gridSize.height.value * density).roundToInt().coerceAtLeast(1)
        val bitmap = createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = activeColor.toArgb()
            style = Paint.Style.FILL
        }
        val inactivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = inactiveColor.toArgb()
            style = Paint.Style.FILL
        }

        val dotSizePx = layout.dotSize.value * density
        val hSpacingPx = layout.horizontalSpacing.value * density
        val vSpacingPx = layout.verticalSpacing.value * density
        val padPx = 0f
        if (dotSizePx <= 0f) {
            return bitmap
        }

        for (index in 1..totalDays) {
            val row = (index - 1) / layout.columns
            val col = (index - 1) % layout.columns
            val x = padPx + col * (dotSizePx + hSpacingPx)
            val y = padPx + row * (dotSizePx + vSpacingPx)
            val radius = dotSizePx / 2f
            val paint = if (index <= currentDay) activePaint else inactivePaint
            canvas.drawCircle(x + radius, y + radius, radius, paint)
        }

        return bitmap
    }

    private fun resolveWidgetSize(
        context: Context,
        glanceId: GlanceId
    ): DpSize {
        val manager = GlanceAppWidgetManager(context)
        val appWidgetId = manager.getAppWidgetId(glanceId)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
        val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
        val fallbackSize = DpSize(minWidth.dp, minHeight.dp)
        val resolvedSize = fallbackSize
        Log.d(
            "YearProgressWidget",
            "Widget options min=${minWidth}x${minHeight} resolved=${resolvedSize.width.value}x${resolvedSize.height.value}"
        )
        return resolvedSize
    }
}

class YearProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = YearProgressWidget()
}
