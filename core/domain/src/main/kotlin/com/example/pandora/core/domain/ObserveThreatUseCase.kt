package com.example.pandora.core.domain

import com.example.pandora.core.model.Threat
import com.example.pandora.core.model.ThreatsResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveThreatUseCase
    @Inject
    constructor(
        private val repository: ThreatRepository,
    ) {
        operator fun invoke(): Flow<ThreatsResult> = repository.observeThreat()
    }
