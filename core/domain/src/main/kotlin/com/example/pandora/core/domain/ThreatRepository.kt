package com.example.pandora.core.domain

import com.example.pandora.core.model.Threat
import kotlinx.coroutines.flow.Flow

interface ThreatRepository {
    fun observeThreat(): Flow<List<Threat>>

    fun getNearbyThreats(): List<Threat>

    suspend fun refreshThreat()
}
