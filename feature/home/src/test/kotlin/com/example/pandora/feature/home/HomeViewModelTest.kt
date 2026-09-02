package com.example.pandora.feature.home

import app.cash.turbine.test
import com.example.pandora.core.domain.ObserveThreatUseCase
import com.example.pandora.core.domain.RefreshThreatUseCase
import com.example.pandora.core.model.Area
import com.example.pandora.core.model.Severity
import com.example.pandora.core.model.Threat
import com.example.pandora.core.testing.FakeClock
import com.example.pandora.core.testing.FakeThreatRepository
import com.example.pandora.core.testing.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @OptIn(ExperimentalTime::class)
    private val fakeClock = FakeClock(Clock.System.now())

    @OptIn(ExperimentalTime::class)
    private val repository = FakeThreatRepository(
        initialThreats = listOf(
            Threat(
                "cholera-abuja",
                "Cholera",
                Severity.LOW,
                Area(9.0021987, 7.3450184),
                listOf("Make sure to only drink water from reliable sources"),
                fakeClock.now(),
                fakeClock.now() + Threat.THREAT_AGEING_WINDOW
            )
        ),
        clock = fakeClock
    )

    @OptIn(ExperimentalTime::class)
    @Test
    fun `initial state observes greeting and refreshes repository`() =
        runTest {
            val viewModel = createViewModel()

            advanceUntilIdle()

            assertThat(viewModel.state.value.greeting).isEqualTo("Make sure to only drink water from reliable sources")
            assertThat(viewModel.state.value.isLoading).isFalse()
            assertThat(repository.refreshCount).isEqualTo(1)
        }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `refresh failure updates state and emits one-shot message`() =
        runTest {
            repository.refreshFailure = IllegalStateException("Refresh unavailable")
            val viewModel = createViewModel()

            viewModel.effects.test {
                advanceUntilIdle()

                assertThat(awaitItem()).isEqualTo(HomeEffect.ShowMessage("Refresh unavailable"))
                assertThat(viewModel.state.value.errorMessage).isEqualTo("Refresh unavailable")
                cancelAndIgnoreRemainingEvents()
            }
        }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `stale threat updates isStale state`() = runTest {
        val staleThreat = Threat(
            "id", "Title", Severity.LOW, Area(0.0, 0.0), listOf("Message"),
            reportedAt = fakeClock.now() - 30.minutes,
            expiresAt = fakeClock.now() - 15.minutes
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        repository.emit(listOf(staleThreat))
        advanceUntilIdle()

        assertThat(viewModel.state.value.isStale).isTrue()
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `threat status flips to stale after ageing window`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.state.value.isStale).isFalse()

        // Advance time beyond the ageing window in the fake clock
        fakeClock.advanceBy(16.minutes)

        // Advance the virtual time in the test dispatcher to resume the delay() in the flow
        testScheduler.advanceTimeBy(16.minutes.inWholeMilliseconds)
        testScheduler.runCurrent()

        assertThat(viewModel.state.value.isStale).isTrue()
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `empty threats list updates hasThreats state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        repository.emit(emptyList())
        advanceUntilIdle()

        assertThat(viewModel.state.value.hasThreats).isFalse()
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `initial state is empty when repository is empty`() = runTest {
        val emptyRepository = FakeThreatRepository(initialThreats = emptyList())
        val viewModel = HomeViewModel(
            observeThreat = ObserveThreatUseCase(emptyRepository),
            refreshThreat = RefreshThreatUseCase(emptyRepository),
        )

        advanceUntilIdle()

        assertThat(viewModel.state.value.hasThreats).isFalse()
    }

    private fun createViewModel() =
        HomeViewModel(
            observeThreat = ObserveThreatUseCase(repository),
            refreshThreat = RefreshThreatUseCase(repository),
        )
}
