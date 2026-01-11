package com.example.sioribi.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class YearProgressWidgetDotPositionsTest {
    @Test
    fun `computeDotPositions returns empty when inputs are invalid`() {
        val specs = DotDrawSpecs(dotSizePx = 0f, horizontalSpacingPx = 1f, verticalSpacingPx = 1f)

        assertThat(computeDotPositions(totalDays = 10, columns = 7, specs = specs)).isEmpty()
        assertThat(computeDotPositions(totalDays = 0, columns = 7, specs = specs.copy(dotSizePx = 2f)))
            .isEmpty()
        assertThat(computeDotPositions(totalDays = 10, columns = 0, specs = specs.copy(dotSizePx = 2f)))
            .isEmpty()
    }

    @Test
    fun `computeDotPositions lays out first row correctly`() {
        val specs = DotDrawSpecs(dotSizePx = 10f, horizontalSpacingPx = 2f, verticalSpacingPx = 3f)

        val positions = computeDotPositions(totalDays = 3, columns = 7, specs = specs)

        assertThat(positions).hasSize(3)
        assertThat(positions[0]).isEqualTo(DotPosition(centerX = 5f, centerY = 5f))
        assertThat(positions[1]).isEqualTo(DotPosition(centerX = 17f, centerY = 5f))
        assertThat(positions[2]).isEqualTo(DotPosition(centerX = 29f, centerY = 5f))
    }

    @Test
    fun `computeDotPositions wraps to next row`() {
        val specs = DotDrawSpecs(dotSizePx = 10f, horizontalSpacingPx = 2f, verticalSpacingPx = 3f)

        val positions = computeDotPositions(totalDays = 3, columns = 2, specs = specs)

        assertThat(positions).hasSize(3)
        assertThat(positions[0]).isEqualTo(DotPosition(centerX = 5f, centerY = 5f))
        assertThat(positions[1]).isEqualTo(DotPosition(centerX = 17f, centerY = 5f))
        assertThat(positions[2]).isEqualTo(DotPosition(centerX = 5f, centerY = 18f))
    }

    @Test
    fun `computeDotDrawCommands returns empty when inputs are invalid`() {
        val specs = DotDrawSpecs(dotSizePx = 0f, horizontalSpacingPx = 1f, verticalSpacingPx = 1f)

        assertThat(
            computeDotDrawCommands(
                totalDays = 10,
                currentDay = 1,
                columns = 7,
                specs = specs,
            ),
        ).isEmpty()
        assertThat(
            computeDotDrawCommands(
                totalDays = 0,
                currentDay = 1,
                columns = 7,
                specs = specs.copy(dotSizePx = 2f),
            ),
        ).isEmpty()
        assertThat(
            computeDotDrawCommands(
                totalDays = 10,
                currentDay = 1,
                columns = 0,
                specs = specs.copy(dotSizePx = 2f),
            ),
        ).isEmpty()
    }

    @Test
    fun `computeDotDrawCommands clamps current day and maps active state`() {
        val specs = DotDrawSpecs(dotSizePx = 10f, horizontalSpacingPx = 2f, verticalSpacingPx = 3f)

        val clampedHigh =
            computeDotDrawCommands(totalDays = 3, currentDay = 10, columns = 7, specs = specs)

        assertThat(clampedHigh).hasSize(3)
        assertThat(clampedHigh.count { it.isActive }).isEqualTo(3)

        val clampedLow =
            computeDotDrawCommands(totalDays = 3, currentDay = -1, columns = 7, specs = specs)

        assertThat(clampedLow).hasSize(3)
        assertThat(clampedLow.count { it.isActive }).isEqualTo(0)

        val commands =
            computeDotDrawCommands(totalDays = 3, currentDay = 1, columns = 7, specs = specs)

        assertThat(commands[0]).isEqualTo(
            DotDrawCommand(centerX = 5f, centerY = 5f, radius = 5f, isActive = true),
        )
        assertThat(commands[1]).isEqualTo(
            DotDrawCommand(centerX = 17f, centerY = 5f, radius = 5f, isActive = false),
        )
        assertThat(commands[2]).isEqualTo(
            DotDrawCommand(centerX = 29f, centerY = 5f, radius = 5f, isActive = false),
        )
    }
}
