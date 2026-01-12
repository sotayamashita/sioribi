package com.example.sioribi.domain

import com.example.sioribi.data.YearProgressRepository

class GetYearProgressUseCase(
    private val yearProgressRepository: YearProgressRepository,
) {
    operator fun invoke(): YearProgress = yearProgressRepository.getYearProgress()
}
