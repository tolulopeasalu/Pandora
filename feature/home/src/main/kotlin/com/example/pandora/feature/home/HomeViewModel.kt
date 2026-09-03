package com.example.pandora.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pandora.core.domain.ObserveThreatUseCase
import com.example.pandora.core.domain.RefreshThreatUseCase
import com.example.pandora.core.model.ThreatStatus
import com.example.pandora.core.model.ThreatsResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val observeThreat: ObserveThreatUseCase,
        private val refreshThreat: RefreshThreatUseCase,
    ) : ViewModel() {
        private val mutableState = MutableStateFlow(HomeUiState())
        val state: StateFlow<HomeUiState> = mutableState

        private val effectChannel = Channel<HomeEffect>(capacity = Channel.BUFFERED)
        val effects = effectChannel.receiveAsFlow()

        init {
            observeThreatState()
            refresh()
        }

        fun onAction(action: HomeAction) {
            when (action) {
                HomeAction.Refresh -> refresh()
            }
        }

        private fun observeThreatState() {
            viewModelScope.launch {
                observeThreat()
                    .catch { error -> handleError(error) }
                    .collect { result ->
                        mutableState.update { state ->
                            when (result) {
                                is ThreatsResult.Success -> {
                                    val threats = result.threats
                                    state.copy(
                                        isLoading = false,
                                        greeting = threats.firstOrNull()?.guidance?.firstOrNull().orEmpty(),
                                        errorMessage = null,
                                        hasThreats = true,
                                        isStale = result.status == ThreatStatus.STALE
                                    )
                                }
                                ThreatsResult.Empty -> {
                                    state.copy(
                                        isLoading = false,
                                        greeting = null,
                                        errorMessage = null,
                                        hasThreats = false,
                                        isStale = false
                                    )
                                }
                            }
                        }
                    }
            }
        }

        private fun refresh() {
            mutableState.update { it.copy(isLoading = true, errorMessage = null) }
            viewModelScope.launch {
                runCatching { refreshThreat() }
                    .onFailure(::handleError)
                mutableState.update { it.copy(isLoading = false) }
            }
        }

        private fun handleError(error: Throwable) {
            val message = error.message?.takeIf(String::isNotBlank) ?: "Something went wrong"
            mutableState.update { it.copy(isLoading = false, errorMessage = message) }
            effectChannel.trySend(HomeEffect.ShowMessage(message))
        }
    }
