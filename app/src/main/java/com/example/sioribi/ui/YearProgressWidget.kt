package com.example.sioribi.ui

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.util.SizeF
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontFamily
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.sioribi.R

private val ACTIVE_COLOR_RES = R.color.widget_dot_active
private val INACTIVE_COLOR_RES = R.color.widget_dot_inactive
private val TEXT_COLOR_RES = R.color.widget_text
private val FOOTER_SPACING = 12.dp
private val FOOTER_HEIGHT = 20.dp

// Minimum columns to keep the grid density consistent (19 dots per row).
private const val MIN_GRID_COLUMNS = 19
private const val DOT_SPACING_RATIO = 0.55f
private const val PADDING_RATIO = 1.8f

class YearProgressWidget : GlanceAppWidget() {
    companion object {
        val KEY_CURRENT_DAY = intPreferencesKey("current_day")
        val KEY_TOTAL_DAYS = intPreferencesKey("total_days")
        val KEY_YEAR = intPreferencesKey("year")
        val KEY_FORMATTED = stringPreferencesKey("formatted")
    }

    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val widgetSize = resolveWidgetSize(context, id)
        provideContent {
            widgetContent(widgetSize)
        }
    }

    @Composable
    private fun widgetContent(widgetSize: DpSize) {
        GlanceTheme {
            val context = LocalContext.current
            val prefs = currentState<Preferences>()
            val values =
                normalizeWidgetValues(
                    WidgetRawValues(
                        currentDay = prefs[KEY_CURRENT_DAY],
                        totalDays = prefs[KEY_TOTAL_DAYS],
                        year = prefs[KEY_YEAR],
                        formatted = prefs[KEY_FORMATTED],
                    ),
                )
            val shouldRefresh = shouldTriggerRefresh(values.year, values.formatted)
            val activeColor = Color(ContextCompat.getColor(context, ACTIVE_COLOR_RES))
            val inactiveColor = Color(ContextCompat.getColor(context, INACTIVE_COLOR_RES))
            val textColorProvider =
                ColorProvider(
                    Color(ContextCompat.getColor(context, TEXT_COLOR_RES)),
                )
            val backgroundColorProvider =
                ColorProvider(
                    Color(ContextCompat.getColor(context, R.color.widget_background)),
                )

            val localSize = LocalSize.current
            val effectiveSize = resolveEffectiveSize(localSize, widgetSize)
            val gridLayout = buildGridLayout(values.totalDays, effectiveSize)

            LaunchedEffect(shouldRefresh) {
                if (shouldRefresh) {
                    WidgetRefreshCoordinatorProvider
                        .from(context)
                        .requestRefresh(RefreshReason.FirstRender)
                }
            }

            val gridSize = buildGridSize(effectiveSize, gridLayout)
            widgetLayout(
                context = context,
                state =
                    buildWidgetLayoutState(
                        values = values,
                        inputs =
                            WidgetLayoutInputs(
                                gridLayout = gridLayout,
                                gridSize = gridSize,
                                backgroundColorProvider = backgroundColorProvider,
                                textColorProvider = textColorProvider,
                                activeColor = activeColor,
                                inactiveColor = inactiveColor,
                            ),
                    ),
            )
        }
    }
}

@Composable
private fun widgetLayout(
    context: Context,
    state: WidgetLayoutState,
) {
    Column(
        modifier =
            GlanceModifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(state.gridLayout.padding)
                .background(state.backgroundColorProvider)
                .clickable(actionRunCallback<ManualRefreshAction>()),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    ) {
        widgetGridImage(context, state)
        Spacer(modifier = GlanceModifier.height(FOOTER_SPACING))
        widgetFooter(state)
    }
}

@Composable
private fun widgetGridImage(
    context: Context,
    state: WidgetLayoutState,
) {
    val bitmap =
        remember(
            state.totalDays,
            state.currentDay,
            state.gridSize,
            state.gridLayout,
        ) {
            buildDotBitmap(
                DotBitmapSpec(
                    context = context,
                    gridSize = state.gridSize,
                    layout = state.gridLayout,
                    totalDays = state.totalDays,
                    currentDay = state.currentDay,
                    activeColor = state.activeColor,
                    inactiveColor = state.inactiveColor,
                ),
            )
        }
    Image(
        provider = ImageProvider(bitmap = bitmap),
        contentDescription = null,
        modifier =
            GlanceModifier
                .fillMaxWidth()
                .height(state.gridSize.height),
        contentScale = ContentScale.FillBounds,
    )
}

@Composable
private fun widgetFooter(state: WidgetLayoutState) {
    Row(
        modifier =
            GlanceModifier
                .fillMaxWidth(),
        horizontalAlignment = Alignment.Horizontal.Start,
    ) {
        Text(
            text = if (state.year == 0) "----" else state.year.toString(),
            style =
                TextStyle(
                    color = state.textColorProvider,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                ),
        )
        Text(
            text = state.formatted,
            modifier = GlanceModifier.fillMaxWidth(),
            style =
                TextStyle(
                    color = state.textColorProvider,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.End,
                ),
        )
    }
}

private data class DotBitmapSpec(
    val context: Context,
    val gridSize: DpSize,
    val layout: GridLayout,
    val totalDays: Int,
    val currentDay: Int,
    val activeColor: Color,
    val inactiveColor: Color,
)

private fun buildDotBitmap(spec: DotBitmapSpec): Bitmap {
    val density = spec.context.resources.displayMetrics.density
    val bitmapSize = computeBitmapSize(spec.gridSize, density)
    val widthPx = bitmapSize.width
    val heightPx = bitmapSize.height
    val bitmap = createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val activePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.activeColor.toArgb()
            style = Paint.Style.FILL
        }
    val inactivePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.inactiveColor.toArgb()
            style = Paint.Style.FILL
        }

    val drawSpecs = computeDotDrawSpecs(spec.layout, density)
    val padPx = 0f
    val commands =
        computeDotDrawCommands(
            totalDays = spec.totalDays,
            currentDay = spec.currentDay,
            columns = spec.layout.columns,
            specs = drawSpecs,
        )
    for (command in commands) {
        val paint = if (command.isActive) activePaint else inactivePaint
        canvas.drawCircle(
            padPx + command.centerX,
            padPx + command.centerY,
            command.radius,
            paint,
        )
    }

    return bitmap
}

internal fun resolveEffectiveSize(
    localSize: DpSize,
    widgetSize: DpSize,
): DpSize =
    if (localSize.width.value > 0f && localSize.height.value > 0f) {
        localSize
    } else {
        widgetSize
    }

internal fun buildGridLayout(
    totalDays: Int,
    size: DpSize,
): GridLayout =
    computeGridLayout(
        GridLayoutConfig(
            totalDays = totalDays,
            size = size,
            footerHeight = FOOTER_HEIGHT,
            footerSpacing = FOOTER_SPACING,
            minColumns = MIN_GRID_COLUMNS,
            spacingRatio = DOT_SPACING_RATIO,
            paddingRatio = PADDING_RATIO,
        ),
    )

internal fun buildGridSize(
    effectiveSize: DpSize,
    gridLayout: GridLayout,
): DpSize =
    DpSize(
        width = (effectiveSize.width - (gridLayout.padding * 2)).coerceAtLeast(0.dp),
        height =
            (effectiveSize.height - (gridLayout.padding * 2) - FOOTER_HEIGHT - FOOTER_SPACING)
                .coerceAtLeast(0.dp),
    )

private fun resolveWidgetSize(
    context: Context,
    glanceId: GlanceId,
): DpSize {
    val manager = GlanceAppWidgetManager(context)
    val appWidgetId = manager.getAppWidgetId(glanceId)
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
    val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
    val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
    val fallbackSize = DpSize(minWidth.dp, minHeight.dp)
    return fallbackSize
}

class YearProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = YearProgressWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetRefreshCoordinatorProvider.from(context).requestRefresh(RefreshReason.Added)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        WidgetRefreshCoordinatorProvider.from(context).requestRefresh(RefreshReason.Updated)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle,
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        WidgetRefreshCoordinatorProvider.from(context).requestRefresh(RefreshReason.OptionsChanged)
    }
}
