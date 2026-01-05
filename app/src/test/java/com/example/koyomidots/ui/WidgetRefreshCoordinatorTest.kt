package com.example.koyomidots.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WidgetRefreshCoordinatorTest {
    @Test
    fun requestRefreshDelegatesToEnqueuer() {
        val enqueuer = FakeEnqueuer()
        val coordinator = WidgetRefreshCoordinator(enqueuer)

        coordinator.requestRefresh(RefreshReason.Manual)

        assertThat(enqueuer.reasons).containsExactly(RefreshReason.Manual)
    }

    private class FakeEnqueuer : WidgetRefreshEnqueuer {
        val reasons = mutableListOf<RefreshReason>()

        override fun enqueueImmediate(reason: RefreshReason) {
            reasons.add(reason)
        }
    }
}
