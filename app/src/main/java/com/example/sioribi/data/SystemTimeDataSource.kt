package com.example.sioribi.data

import java.time.Clock
import java.time.LocalDate

class SystemTimeDataSource(
    private val clock: Clock = Clock.systemDefaultZone(),
) : TimeDataSource {
    override fun today(): LocalDate = LocalDate.now(clock)
}
