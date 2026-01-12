package com.example.sioribi.ui

import com.example.sioribi.domain.GetYearProgressUseCase
import com.example.sioribi.domain.YearProgress
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test

class WidgetUpdateCoordinatorTest {
    @Test
    fun `update writes renders and logs with state and reason`() =
        runBlocking {
            val progress =
                YearProgress(
                    currentDay = 2,
                    totalDays = 365,
                    year = 2026,
                )
            val mapper = YearProgressUiStateMapper()
            val state = mapper.map(progress)
            val calls = mutableListOf<String>()
            val coordinator =
                WidgetUpdateCoordinator(
                    getYearProgressUseCase = GetYearProgressUseCase(FakeRepository(progress)),
                    mapper = mapper,
                    stateWriter =
                        FakeStateWriter { written ->
                            assertThat(written).isEqualTo(state)
                            calls.add("write")
                        },
                    renderer =
                        FakeRenderer {
                            calls.add("render")
                        },
                    logger =
                        FakeLogger { loggedState, reason ->
                            calls.add("log:${reason.name}")
                            assertThat(loggedState).isEqualTo(state)
                        },
                )

            coordinator.update(RefreshReason.Manual)

            assertThat(calls).containsExactly("write", "render", "log:Manual").inOrder()
        }

    private class FakeRepository(
        private val model: YearProgress,
    ) : com.example.sioribi.data.YearProgressRepository {
        override fun getYearProgress(): YearProgress = model
    }

    private class FakeStateWriter(
        private val onWrite: (YearProgressUiState) -> Unit,
    ) : WidgetStateWriter {
        override suspend fun write(state: YearProgressUiState) {
            onWrite(state)
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
        private val onLog: (YearProgressUiState, RefreshReason) -> Unit,
    ) : WidgetUpdateLogger {
        override fun log(
            state: YearProgressUiState,
            reason: RefreshReason,
        ) {
            onLog(state, reason)
        }
    }
}
