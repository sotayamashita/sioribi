package com.example.sioribi.ui

import android.util.Log
import com.example.sioribi.domain.GetYearProgressUseCase
import com.example.sioribi.domain.YearProgressModel

class WidgetUpdateCoordinator(
    private val getYearProgressUseCase: GetYearProgressUseCase,
    private val stateWriter: WidgetStateWriter,
    private val renderer: WidgetRenderer,
    private val logger: WidgetUpdateLogger = DefaultWidgetUpdateLogger(),
) {
    suspend fun update(reason: RefreshReason) {
        val model = getYearProgressUseCase.execute()
        stateWriter.write(model)
        renderer.render()
        logger.log(model, reason)
    }
}

interface WidgetUpdateLogger {
    fun log(
        model: YearProgressModel,
        reason: RefreshReason,
    )
}

class DefaultWidgetUpdateLogger : WidgetUpdateLogger {
    override fun log(
        model: YearProgressModel,
        reason: RefreshReason,
    ) {
        Log.d("WidgetUpdateCoordinator", buildWidgetUpdateLogMessage(model, reason.name))
    }
}
