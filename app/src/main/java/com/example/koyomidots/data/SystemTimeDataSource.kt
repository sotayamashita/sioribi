package com.example.koyomidots.data

import java.time.LocalDate

class SystemTimeDataSource : TimeDataSource {
    override fun today(): LocalDate = LocalDate.now()
}
