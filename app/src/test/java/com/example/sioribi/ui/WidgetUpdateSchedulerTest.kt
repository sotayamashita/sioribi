package com.example.sioribi.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class WidgetUpdateSchedulerTest {
    @Test
    fun `computeInitialDelayMillis returns one second before midnight`() {
        val clock = Clock.fixed(Instant.parse("2026-01-01T23:59:59Z"), ZoneId.of("UTC"))

        val delay = computeInitialDelayMillis(clock)

        assertThat(delay).isEqualTo(1000L)
    }

    @Test
    fun `computeInitialDelayMillis returns full day at midnight`() {
        val clock = Clock.fixed(Instant.parse("2026-01-02T00:00:00Z"), ZoneId.of("UTC"))

        val delay = computeInitialDelayMillis(clock)

        assertThat(delay).isEqualTo(86_400_000L)
    }

    @Test
    fun `computeInitialDelayMillis respects clock zone`() {
        val clock = Clock.fixed(Instant.parse("2026-01-01T21:00:00Z"), ZoneId.of("+02:00"))

        val delay = computeInitialDelayMillis(clock)

        assertThat(delay).isEqualTo(3_600_000L)
    }
}
