package com.example.pandora.core.domain

import javax.inject.Inject

class RefreshThreatUseCase
    @Inject
    constructor(
        private val repository: ThreatRepository,
    ) {
        suspend operator fun invoke() = repository.refreshThreat()
    }
