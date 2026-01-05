package com.example.sioribi.data

import java.time.LocalDate

interface TimeDataSource {
    fun today(): LocalDate
}
