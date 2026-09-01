package com.example.pandora.core.testing
import com.example.pandora.core.domain.ThreatRepository
import com.example.pandora.core.model.Area
import com.example.pandora.core.model.Severity
import com.example.pandora.core.model.Threat
import com.example.pandora.core.model.ThreatsResult
import com.example.pandora.core.model.collectionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class FakeThreatRepository constructor(
    initialThreats: List<Threat> = emptyList(),
    private val clock: Clock = Clock.System
) : ThreatRepository {

    private val threatsFlow = MutableStateFlow<ThreatsResult>(
        if (initialThreats.isEmpty()) {
            ThreatsResult.Empty
        } else {
            ThreatsResult.Success(
                threats = initialThreats,
                status = initialThreats.collectionStatus(clock.now()),
            )
        }
    )

    var refreshCount: Int = 0
        private set
    var refreshFailure: Throwable? = null



    fun emit(values: List<Threat>) {
        threatsFlow.value = if (values.isEmpty()) {
            ThreatsResult.Empty
        } else {
            ThreatsResult.Success(
                threats = values,
                status = values.collectionStatus(clock.now()),
            )
        }
    }

    override fun observeThreat(): Flow<ThreatsResult> = threatsFlow

    override fun getNearbyThreats(): List<Threat> {
        return when (val result = threatsFlow.value) {
            is ThreatsResult.Success -> result.threats
            ThreatsResult.Empty -> emptyList()
        }
    }

    override suspend fun refreshThreat() {
        refreshCount += 1
        refreshFailure?.let { throw it }
    }
}
