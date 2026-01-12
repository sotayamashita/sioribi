package com.example.sioribi.ui

import com.example.sioribi.domain.GetYearProgressUseCase
import com.example.sioribi.domain.YearProgressModel
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test

class WidgetUpdateCoordinatorTest {
    @Test
    fun `update writes renders and logs with model and reason`() =
        runBlocking {
            val model =
                YearProgressModel(
                    currentDay = 2,
                    totalDays = 365,
                    year = 2026,
                    progressPercentage = 1,
                    formattedString = "2/365",
                )
            val calls = mutableListOf<String>()
            val coordinator =
                WidgetUpdateCoordinator(
                    getYearProgressUseCase = GetYearProgressUseCase(FakeRepository(model)),
                    stateWriter =
                        FakeStateWriter {
                            calls.add("write")
                        },
                    renderer =
                        FakeRenderer {
                            calls.add("render")
                        },
                    logger =
                        FakeLogger { loggedModel, reason ->
                            calls.add("log:${reason.name}")
                            assertThat(loggedModel).isEqualTo(model)
                        },
                )

            coordinator.update(RefreshReason.Manual)

            assertThat(calls).containsExactly("write", "render", "log:Manual").inOrder()
        }

    private class FakeRepository(
        private val model: YearProgressModel,
    ) : com.example.sioribi.data.YearProgressRepository {
        override fun getYearProgress(): YearProgressModel = model
    }

    private class FakeStateWriter(
        private val onWrite: () -> Unit,
    ) : WidgetStateWriter {
        override suspend fun write(model: YearProgressModel) {
            onWrite()
        }
    }

    private class FakeRenderer(
        private val onRender: () -> Unit,
    ) : WidgetRenderer {
        override suspend fun render() {
            onRender()
        }
    }

    private class FakeLogger(
        private val onLog: (YearProgressModel, RefreshReason) -> Unit,
    ) : WidgetUpdateLogger {
        override fun log(
            model: YearProgressModel,
            reason: RefreshReason,
        ) {
            onLog(model, reason)
        }
    }
}
