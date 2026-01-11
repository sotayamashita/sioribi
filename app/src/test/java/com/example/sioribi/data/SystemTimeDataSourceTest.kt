package com.example.sioribi.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class SystemTimeDataSourceTest {
    @Test
    fun `today returns date from injected clock`() {
        val fixedClock = Clock.fixed(Instant.parse("2026-03-14T00:00:00Z"), ZoneId.of("UTC"))
        val dataSource = SystemTimeDataSource(fixedClock)

        val result = dataSource.today()

        assertThat(result).isEqualTo(LocalDate.of(2026, 3, 14))
    }
}
