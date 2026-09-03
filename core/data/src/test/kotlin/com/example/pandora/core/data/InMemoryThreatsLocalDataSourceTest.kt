package com.example.pandora.core.data

import app.cash.turbine.test
import com.example.pandora.core.model.Threat
import com.example.pandora.core.model.ThreatStatus
import com.example.pandora.core.model.ThreatsResult
import com.example.pandora.core.testing.FakeClock
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class InMemoryThreatsLocalDataSourceTest {

    private val fakeClock = FakeClock(Clock.System.now())
    private val dataSource = InMemoryThreatsLocalDataSource(fakeClock)

    @Test
    fun `initially emits empty result`() = runTest {
        dataSource.observeThreat().test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(ThreatsResult.Empty::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refreshThreat seeds data and emits Success`() = runTest {
        dataSource.observeThreat().test {
            assertThat(awaitItem()).isInstanceOf(ThreatsResult.Empty::class.java)

            dataSource.refreshThreat()

            val success = awaitItem() as ThreatsResult.Success
            assertThat(success.threats).isNotEmpty()
            assertThat(success.status).isEqualTo(ThreatStatus.FRESH)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `threat status flips to STALE automatically when time passes`() = runTest {
        dataSource.refreshThreat()

        dataSource.observeThreat().test {
            val initial = awaitItem() as ThreatsResult.Success
            assertThat(initial.status).isEqualTo(ThreatStatus.FRESH)

            // Advance time beyond the ageing window
            fakeClock.advanceBy(Threat.THREAT_AGEING_WINDOW + 1.minutes)

            // The loop in observeThreat uses delay(), so we need to advance the test scheduler
            testScheduler.advanceTimeBy((Threat.THREAT_AGEING_WINDOW + 1.minutes).inWholeMilliseconds)

            val stale = awaitItem() as ThreatsResult.Success
            assertThat(stale.status).isEqualTo(ThreatStatus.STALE)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refreshing after stale updates status to FRESH`() = runTest {
        dataSource.refreshThreat()
        fakeClock.advanceBy(Threat.THREAT_AGEING_WINDOW + 1.minutes)

        dataSource.observeThreat().test {
            val stale = awaitItem() as ThreatsResult.Success
            assertThat(stale.status).isEqualTo(ThreatStatus.STALE)

            dataSource.refreshThreat()

            val fresh = awaitItem() as ThreatsResult.Success
            assertThat(fresh.status).isEqualTo(ThreatStatus.FRESH)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refreshThreat rotates data when already seeded`() = runTest {
        dataSource.refreshThreat()
        val firstThreats = dataSource.getNearbyThreats()
        val firstReportedAt = firstThreats.first().reportedAt

        fakeClock.advanceBy(1.minutes)
        dataSource.refreshThreat()

        val secondThreats = dataSource.getNearbyThreats()
        assertThat(secondThreats.first().reportedAt).isGreaterThan(firstReportedAt)
        // Verify internal index rotation (though hard to see without more introspection,
        // we at least verify the update logic)
    }
}
