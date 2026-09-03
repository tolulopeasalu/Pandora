package com.example.pandora.core.model

sealed interface ThreatsResult {
    data class Success(
        val threats: List<Threat>,
        val status: ThreatStatus,
    ) : ThreatsResult
    data object Empty : ThreatsResult
}
