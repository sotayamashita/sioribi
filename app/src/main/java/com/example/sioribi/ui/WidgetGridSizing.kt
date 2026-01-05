package com.example.sioribi.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.min

internal data class GridLayout(
    val columns: Int,
    val rows: Int,
    val dotSize: Dp,
    val horizontalSpacing: Dp,
    val verticalSpacing: Dp,
    val padding: Dp
)

internal fun computeGridLayout(
    totalDays: Int,
    size: DpSize,
    footerHeight: Dp,
    footerSpacing: Dp,
    minColumns: Int,
    spacingRatio: Float,
    paddingRatio: Float
): GridLayout {
    val availableWidth = size.width.coerceAtLeast(0.dp)
    val availableHeight = (size.height - footerHeight - footerSpacing)
        .coerceAtLeast(0.dp)
    val availableWidthDp = availableWidth.value
    val availableHeightDp = availableHeight.value
    if (availableWidthDp <= 0f || availableHeightDp <= 0f) {
        val fallbackColumns = minColumns.coerceAtLeast(1)
        val fallbackRows = ceil(totalDays / fallbackColumns.toFloat()).toInt().coerceAtLeast(1)
        return GridLayout(
            columns = fallbackColumns,
            rows = fallbackRows,
            dotSize = 1.dp,
            horizontalSpacing = 0.dp,
            verticalSpacing = 0.dp,
            padding = 0.dp
        )
    }

    val resolvedMinColumns = minColumns.coerceAtLeast(1)
    val maxColumns = totalDays.coerceAtLeast(resolvedMinColumns)
    var maxDotSize = 1f

    for (columns in resolvedMinColumns..maxColumns) {
        val rows = ceil(totalDays / columns.toFloat()).toInt().coerceAtLeast(1)
        val widthUnits = columns + (columns - 1) * spacingRatio + (paddingRatio * 2)
        val heightUnits = rows + (rows - 1) * spacingRatio + (paddingRatio * 2)
        val candidateDotSize = min(
            availableWidthDp / widthUnits,
            availableHeightDp / heightUnits
        ).coerceAtLeast(1f)
        if (candidateDotSize > maxDotSize) {
            maxDotSize = candidateDotSize
        }
    }

    val tolerance = 0.03f
    var bestColumns = resolvedMinColumns
    var bestRows = ceil(totalDays / resolvedMinColumns.toFloat()).toInt().coerceAtLeast(1)
    var bestDotSize = maxDotSize

    for (columns in resolvedMinColumns..maxColumns) {
        val rows = ceil(totalDays / columns.toFloat()).toInt().coerceAtLeast(1)
        val widthUnits = columns + (columns - 1) * spacingRatio + (paddingRatio * 2)
        val heightUnits = rows + (rows - 1) * spacingRatio + (paddingRatio * 2)
        val candidateDotSize = min(
            availableWidthDp / widthUnits,
            availableHeightDp / heightUnits
        ).coerceAtLeast(1f)
        if (candidateDotSize >= maxDotSize * (1f - tolerance) && columns > bestColumns) {
            bestColumns = columns
            bestRows = rows
            bestDotSize = candidateDotSize
        }
    }

    val spacing = bestDotSize * spacingRatio
    val padding = bestDotSize * paddingRatio
    return GridLayout(
        columns = bestColumns,
        rows = bestRows,
        dotSize = bestDotSize.dp,
        horizontalSpacing = spacing.dp,
        verticalSpacing = spacing.dp,
        padding = padding.dp
    )
}

internal fun pickLargestSize(
    sizes: List<DpSize>?,
    fallback: DpSize
): DpSize {
    val resolved = sizes
        ?.filter { size -> size.width.value > 0f && size.height.value > 0f }
        ?.maxByOrNull { size -> size.width.value * size.height.value }
    return resolved ?: fallback
}
