package com.example.sioribi.ui

import android.content.Context
import android.content.Intent
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Test

class WidgetRefreshReceiverTest {
    private val originalProvider = WidgetRefreshCoordinatorProvider.provider
    private val enqueuer = FakeEnqueuer()
    private val coordinator = WidgetRefreshCoordinator(enqueuer)
    private val context: Context = mockk(relaxed = true)

    @Before
    fun setUp() {
        enqueuer.reasons.clear()
        WidgetRefreshCoordinatorProvider.provider = { coordinator }
    }

    @After
    fun tearDown() {
        WidgetRefreshCoordinatorProvider.provider = originalProvider
    }

    @Test
    fun timeChangeActionsTriggerTimeChangedReason() {
        val receiver = WidgetRefreshReceiver()

        receiver.onReceive(context, intentWithAction(Intent.ACTION_DATE_CHANGED))
        receiver.onReceive(context, intentWithAction(Intent.ACTION_TIME_CHANGED))
        receiver.onReceive(context, intentWithAction(Intent.ACTION_TIMEZONE_CHANGED))

        assertThat(enqueuer.reasons)
            .containsExactly(
                RefreshReason.TimeChanged,
                RefreshReason.TimeChanged,
                RefreshReason.TimeChanged,
            ).inOrder()
    }

    @Test
    fun bootCompletedTriggersBootReason() {
        val receiver = WidgetRefreshReceiver()

        receiver.onReceive(context, intentWithAction(Intent.ACTION_BOOT_COMPLETED))

        assertThat(enqueuer.reasons).containsExactly(RefreshReason.Boot)
    }

    @Test
    fun packageReplacedTriggersPackageReplacedReason() {
        val receiver = WidgetRefreshReceiver()

        receiver.onReceive(context, intentWithAction(Intent.ACTION_MY_PACKAGE_REPLACED))

        assertThat(enqueuer.reasons).containsExactly(RefreshReason.PackageReplaced)
    }

    @Test
    fun unknownActionDoesNotEnqueue() {
        val receiver = WidgetRefreshReceiver()

        receiver.onReceive(context, intentWithAction("com.example.UNKNOWN"))

        assertThat(enqueuer.reasons).isEmpty()
    }

    private class FakeEnqueuer : WidgetRefreshEnqueuer {
        val reasons = mutableListOf<RefreshReason>()

        override fun enqueueImmediate(reason: RefreshReason) {
            reasons.add(reason)
        }
    }

    private fun intentWithAction(action: String): Intent {
        val intent = mockk<Intent>(relaxed = true)
        every { intent.action } returns action
        return intent
    }
}
