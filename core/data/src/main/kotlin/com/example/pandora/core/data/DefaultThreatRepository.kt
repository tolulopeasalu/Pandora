package com.example.pandora.core.data

import com.example.pandora.core.domain.ThreatRepository
import com.example.pandora.core.model.Threat
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DefaultThreatRepository
    @Inject
    constructor(
        private val localDataSource: ThreatsLocalDataSource,
    ) : ThreatRepository {
        override fun observeThreat(): Flow<Threat> = localDataSource.observeThreat()
    override fun getNearbyThreats(): List<Threat> = localDataSource.getNearbyThreats()

    override suspend fun refreshThreat() = localDataSource.refreshThreat()
    }
