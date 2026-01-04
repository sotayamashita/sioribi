package com.example.koyomidots.data

import java.time.LocalDate

interface TimeDataSource {
    fun today(): LocalDate
}
