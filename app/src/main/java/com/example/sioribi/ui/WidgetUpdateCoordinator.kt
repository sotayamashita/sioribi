package com.example.sioribi.ui

import android.util.Log
import com.example.sioribi.domain.GetYearProgressUseCase

class WidgetUpdateCoordinator(
    private val getYearProgressUseCase: GetYearProgressUseCase,
    private val mapper: YearProgressUiStateMapper,
    private val stateWriter: WidgetStateWriter,
    private val renderer: WidgetRenderer,
    private val logger: WidgetUpdateLogger = DefaultWidgetUpdateLogger(),
) {
    suspend fun update(reason: RefreshReason) {
        val progress = getYearProgressUseCase()
        val state = mapper.map(progress)
        stateWriter.write(state)
        renderer.render()
        logger.log(state, reason)
    }
}

interface WidgetUpdateLogger {
    fun log(
        state: YearProgressUiState,
        reason: RefreshReason,
    )
}

class DefaultWidgetUpdateLogger : WidgetUpdateLogger {
    override fun log(
        state: YearProgressUiState,
        reason: RefreshReason,
    ) {
        Log.d("WidgetUpdateCoordinator", buildWidgetUpdateLogMessage(state, reason.name))
    }
}
