package com.example.koyomidots.ui

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
    val verticalSpacing: Dp
)

internal fun computeGridLayout(
    totalDays: Int,
    size: DpSize,
    padding: Dp,
    footerHeight: Dp,
    footerSpacing: Dp
): GridLayout {
    val availableWidth = (size.width - (padding * 2)).coerceAtLeast(0.dp)
    val availableHeight = (size.height - (padding * 2) - footerHeight - footerSpacing)
        .coerceAtLeast(0.dp)
    val availableWidthDp = availableWidth.value
    val availableHeightDp = availableHeight.value
    if (availableWidthDp <= 0f || availableHeightDp <= 0f) {
        return GridLayout(
            columns = 1,
            rows = totalDays,
            dotSize = 1.dp,
            horizontalSpacing = 0.dp,
            verticalSpacing = 0.dp
        )
    }

    var bestColumns = 1
    var bestRows = totalDays
    var bestDotSize = 0f
    var bestHSpacing = 0f
    var bestVSpacing = 0f
    for (columns in 1..totalDays) {
        val rows = ceil(totalDays.toFloat() / columns.toFloat()).toInt()
        val dotSizeCandidate = min(
            availableWidthDp / columns.toFloat(),
            availableHeightDp / rows.toFloat()
        )
        if (dotSizeCandidate <= 0f) {
            continue
        }
        val hSpacingCandidate = if (columns > 1) {
            (availableWidthDp - (dotSizeCandidate * columns.toFloat())) / (columns - 1).toFloat()
        } else {
            0f
        }
        val vSpacingCandidate = if (rows > 1) {
            (availableHeightDp - (dotSizeCandidate * rows.toFloat())) / (rows - 1).toFloat()
        } else {
            0f
        }
        if (hSpacingCandidate < 0f || vSpacingCandidate < 0f) {
            continue
        }
        if (dotSizeCandidate > bestDotSize) {
            bestDotSize = dotSizeCandidate
            bestColumns = columns
            bestRows = rows
            bestHSpacing = hSpacingCandidate
            bestVSpacing = vSpacingCandidate
        }
    }

    val resolvedDotSize = bestDotSize.coerceAtLeast(0.5f)
    return GridLayout(
        columns = bestColumns,
        rows = bestRows,
        dotSize = resolvedDotSize.dp,
        horizontalSpacing = bestHSpacing.dp,
        verticalSpacing = bestVSpacing.dp
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
