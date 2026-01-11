package com.example.sioribi.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.glance.unit.ColorProvider

internal const val DEFAULT_TOTAL_DAYS = 365
internal const val DEFAULT_FORMATTED = "0/0"

internal data class WidgetDefaults(
    val totalDays: Int = DEFAULT_TOTAL_DAYS,
    val formatted: String = DEFAULT_FORMATTED,
)

internal data class WidgetRawValues(
    val currentDay: Int?,
    val totalDays: Int?,
    val year: Int?,
    val formatted: String?,
)

internal data class WidgetValueState(
    val currentDay: Int,
    val totalDays: Int,
    val year: Int,
    val formatted: String,
)

internal data class WidgetLayoutInputs(
    val gridLayout: GridLayout,
    val gridSize: DpSize,
    val backgroundColorProvider: ColorProvider,
    val textColorProvider: ColorProvider,
    val activeColor: Color,
    val inactiveColor: Color,
)

internal data class WidgetLayoutState(
    val gridLayout: GridLayout,
    val gridSize: DpSize,
    val backgroundColorProvider: ColorProvider,
    val textColorProvider: ColorProvider,
    val year: Int,
    val formatted: String,
    val totalDays: Int,
    val currentDay: Int,
    val activeColor: Color,
    val inactiveColor: Color,
)

internal fun normalizeWidgetValues(
    rawValues: WidgetRawValues,
    defaults: WidgetDefaults = WidgetDefaults(),
): WidgetValueState {
    val normalizedTotalDays = rawValues.totalDays?.takeIf { it > 0 } ?: defaults.totalDays
    val normalizedCurrentDay = rawValues.currentDay?.coerceAtLeast(0) ?: 0
    val normalizedYear = rawValues.year ?: 0
    val normalizedFormatted = rawValues.formatted?.takeIf { it.isNotBlank() } ?: defaults.formatted
    return WidgetValueState(
        currentDay = normalizedCurrentDay,
        totalDays = normalizedTotalDays,
        year = normalizedYear,
        formatted = normalizedFormatted,
    )
}

internal fun shouldTriggerRefresh(
    year: Int,
    formatted: String,
): Boolean = year == 0 && formatted == DEFAULT_FORMATTED

internal fun buildWidgetLayoutState(
    values: WidgetValueState,
    inputs: WidgetLayoutInputs,
): WidgetLayoutState =
    WidgetLayoutState(
        gridLayout = inputs.gridLayout,
        gridSize = inputs.gridSize,
        backgroundColorProvider = inputs.backgroundColorProvider,
        textColorProvider = inputs.textColorProvider,
        year = values.year,
        formatted = values.formatted,
        totalDays = values.totalDays,
        currentDay = values.currentDay,
        activeColor = inputs.activeColor,
        inactiveColor = inputs.inactiveColor,
    )
