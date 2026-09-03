package com.example.pandora.core.domain

import com.example.pandora.core.model.Threat
import com.example.pandora.core.model.ThreatsResult
import kotlinx.coroutines.flow.Flow

interface ThreatRepository {
    fun observeThreat(): Flow<ThreatsResult>

    fun getNearbyThreats(): List<Threat>

    suspend fun refreshThreat()
}
