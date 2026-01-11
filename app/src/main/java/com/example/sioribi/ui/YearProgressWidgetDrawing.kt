package com.example.sioribi.ui

import androidx.compose.ui.unit.DpSize
import kotlin.math.roundToInt

internal data class BitmapSize(
    val width: Int,
    val height: Int,
)

internal data class DotDrawSpecs(
    val dotSizePx: Float,
    val horizontalSpacingPx: Float,
    val verticalSpacingPx: Float,
)

internal fun computeBitmapSize(
    gridSize: DpSize,
    density: Float,
): BitmapSize {
    val widthPx = (gridSize.width.value * density).roundToInt().coerceAtLeast(1)
    val heightPx = (gridSize.height.value * density).roundToInt().coerceAtLeast(1)
    return BitmapSize(width = widthPx, height = heightPx)
}

internal fun computeDotDrawSpecs(
    layout: GridLayout,
    density: Float,
): DotDrawSpecs =
    DotDrawSpecs(
        dotSizePx = layout.dotSize.value * density,
        horizontalSpacingPx = layout.horizontalSpacing.value * density,
        verticalSpacingPx = layout.verticalSpacing.value * density,
    )
