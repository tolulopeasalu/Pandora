package com.example.pandora.feature.home

data class HomeUiState(
    val isLoading: Boolean = true,
    val greeting: String? = null,
    val errorMessage: String? = null,
    val hasThreats: Boolean = false,
    val isStale: Boolean = false
)

sealed interface HomeAction {
    data object Refresh : HomeAction
}

sealed interface HomeEffect {
    data class ShowMessage(
        val message: String,
    ) : HomeEffect
}
