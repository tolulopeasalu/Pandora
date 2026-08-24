package com.example.pandora.feature.home

import app.cash.turbine.test
import com.example.pandora.core.domain.ObserveThreatUseCase
import com.example.pandora.core.domain.RefreshThreatUseCase
import com.example.pandora.core.model.Threat
import com.example.pandora.core.testing.FakeThreatRepository
import com.example.pandora.core.testing.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @OptIn(ExperimentalTime::class)
    private val repository = FakeThreatRepository()

    @Test
    fun `initial state observes greeting and refreshes repository`() =
        runTest {
            val viewModel = createViewModel()

            advanceUntilIdle()

            assertThat(viewModel.state.value.greeting).isEqualTo("Make sure to only drink water from reliable sources")
            assertThat(viewModel.state.value.isLoading).isFalse()
            assertThat(repository.refreshCount).isEqualTo(1)
        }

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

    private fun createViewModel() =
        HomeViewModel(
            observeThreat = ObserveThreatUseCase(repository),
            refreshThreat = RefreshThreatUseCase(repository),
        )
}
