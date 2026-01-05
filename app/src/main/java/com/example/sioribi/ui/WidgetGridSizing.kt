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
    val padding: Dp,
)

internal data class GridLayoutConfig(
    val totalDays: Int,
    val size: DpSize,
    val footerHeight: Dp,
    val footerSpacing: Dp,
    val minColumns: Int,
    val spacingRatio: Float,
    val paddingRatio: Float,
)

private const val DEFAULT_TOLERANCE = 0.03f

internal fun computeGridLayout(config: GridLayoutConfig): GridLayout {
    val availableWidth = config.size.width.coerceAtLeast(0.dp)
    val availableHeight =
        (config.size.height - config.footerHeight - config.footerSpacing)
            .coerceAtLeast(0.dp)
    val availableWidthDp = availableWidth.value
    val availableHeightDp = availableHeight.value
    if (availableWidthDp <= 0f || availableHeightDp <= 0f) {
        val fallbackColumns = config.minColumns.coerceAtLeast(1)
        val fallbackRows =
            ceil(config.totalDays / fallbackColumns.toFloat()).toInt().coerceAtLeast(1)
        return GridLayout(
            columns = fallbackColumns,
            rows = fallbackRows,
            dotSize = 1.dp,
            horizontalSpacing = 0.dp,
            verticalSpacing = 0.dp,
            padding = 0.dp,
        )
    }

    val resolvedMinColumns = config.minColumns.coerceAtLeast(1)
    val maxColumns = config.totalDays.coerceAtLeast(resolvedMinColumns)
    val sizingInput =
        LayoutSizingInput(
            totalDays = config.totalDays,
            availableWidthDp = availableWidthDp,
            availableHeightDp = availableHeightDp,
            spacingRatio = config.spacingRatio,
            paddingRatio = config.paddingRatio,
        )
    val maxDotSize =
        findMaxDotSize(
            input = sizingInput,
            resolvedMinColumns = resolvedMinColumns,
            maxColumns = maxColumns,
        )
    val bestFit =
        findBestFit(
            input = sizingInput,
            resolvedMinColumns = resolvedMinColumns,
            maxColumns = maxColumns,
            maxDotSize = maxDotSize,
        )
    val spacing = bestFit.dotSize * config.spacingRatio
    val padding = bestFit.dotSize * config.paddingRatio
    return GridLayout(
        columns = bestFit.columns,
        rows = bestFit.rows,
        dotSize = bestFit.dotSize.dp,
        horizontalSpacing = spacing.dp,
        verticalSpacing = spacing.dp,
        padding = padding.dp,
    )
}

private data class BestFit(
    val columns: Int,
    val rows: Int,
    val dotSize: Float,
)

private data class LayoutSizingInput(
    val totalDays: Int,
    val availableWidthDp: Float,
    val availableHeightDp: Float,
    val spacingRatio: Float,
    val paddingRatio: Float,
)

private fun findMaxDotSize(
    input: LayoutSizingInput,
    resolvedMinColumns: Int,
    maxColumns: Int,
): Float {
    var maxDotSize = 1f
    for (columns in resolvedMinColumns..maxColumns) {
        val candidateDotSize =
            computeCandidateDotSize(
                input = input,
                columns = columns,
            )
        if (candidateDotSize > maxDotSize) {
            maxDotSize = candidateDotSize
        }
    }
    return maxDotSize
}

private fun findBestFit(
    input: LayoutSizingInput,
    resolvedMinColumns: Int,
    maxColumns: Int,
    maxDotSize: Float,
): BestFit {
    val tolerance = DEFAULT_TOLERANCE
    var bestColumns = resolvedMinColumns
    var bestRows = ceil(input.totalDays / resolvedMinColumns.toFloat()).toInt().coerceAtLeast(1)
    var bestDotSize = maxDotSize

    for (columns in resolvedMinColumns..maxColumns) {
        val candidateDotSize =
            computeCandidateDotSize(
                input = input,
                columns = columns,
            )
        if (candidateDotSize >= maxDotSize * (1f - tolerance) && columns > bestColumns) {
            bestColumns = columns
            bestRows = ceil(input.totalDays / columns.toFloat()).toInt().coerceAtLeast(1)
            bestDotSize = candidateDotSize
        }
    }

    return BestFit(columns = bestColumns, rows = bestRows, dotSize = bestDotSize)
}

private fun computeCandidateDotSize(
    input: LayoutSizingInput,
    columns: Int,
): Float {
    val rows = ceil(input.totalDays / columns.toFloat()).toInt().coerceAtLeast(1)
    val widthUnits = columns + (columns - 1) * input.spacingRatio + (input.paddingRatio * 2)
    val heightUnits = rows + (rows - 1) * input.spacingRatio + (input.paddingRatio * 2)
    return min(
        input.availableWidthDp / widthUnits,
        input.availableHeightDp / heightUnits,
    ).coerceAtLeast(1f)
}

internal fun pickLargestSize(
    sizes: List<DpSize>?,
    fallback: DpSize,
): DpSize {
    val resolved =
        sizes
            ?.filter { size -> size.width.value > 0f && size.height.value > 0f }
            ?.maxByOrNull { size -> size.width.value * size.height.value }
    return resolved ?: fallback
}
