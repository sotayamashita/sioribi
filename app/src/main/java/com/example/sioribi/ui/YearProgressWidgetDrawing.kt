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

internal data class DotPosition(
    val centerX: Float,
    val centerY: Float,
)

internal data class DotDrawCommand(
    val centerX: Float,
    val centerY: Float,
    val radius: Float,
    val isActive: Boolean,
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

internal fun computeDotPositions(
    totalDays: Int,
    columns: Int,
    specs: DotDrawSpecs,
): List<DotPosition> {
    if (totalDays <= 0 || columns <= 0 || specs.dotSizePx <= 0f) {
        return emptyList()
    }
    val dotPositions = ArrayList<DotPosition>(totalDays)
    val radius = specs.dotSizePx / 2f
    for (index in 1..totalDays) {
        val row = (index - 1) / columns
        val col = (index - 1) % columns
        val x = col * (specs.dotSizePx + specs.horizontalSpacingPx)
        val y = row * (specs.dotSizePx + specs.verticalSpacingPx)
        val centerX = x + radius
        val centerY = y + radius
        dotPositions.add(DotPosition(centerX = centerX, centerY = centerY))
    }
    return dotPositions
}

internal fun computeDotDrawCommands(
    totalDays: Int,
    currentDay: Int,
    columns: Int,
    specs: DotDrawSpecs,
): List<DotDrawCommand> {
    if (totalDays <= 0 || columns <= 0 || specs.dotSizePx <= 0f) {
        return emptyList()
    }
    val safeCurrentDay = currentDay.coerceIn(0, totalDays)
    val radius = specs.dotSizePx / 2f
    val positions = computeDotPositions(totalDays = totalDays, columns = columns, specs = specs)
    val commands = ArrayList<DotDrawCommand>(positions.size)
    for ((index, position) in positions.withIndex()) {
        val dayIndex = index + 1
        commands.add(
            DotDrawCommand(
                centerX = position.centerX,
                centerY = position.centerY,
                radius = radius,
                isActive = dayIndex <= safeCurrentDay,
            ),
        )
    }
    return commands
}
