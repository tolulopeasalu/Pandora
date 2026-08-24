package com.example.pandora.core.testing
import com.example.pandora.core.domain.ThreatRepository
import com.example.pandora.core.model.Area
import com.example.pandora.core.model.Severity
import com.example.pandora.core.model.Threat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class FakeThreatRepository constructor(
    initialThreat: Threat = Threat(
        "cholera-abuja",
        "Cholera",
        Severity.LOW,
        Area(9.0021987, 7.3450184),
        listOf("Make sure to only drink water from reliable sources"),
        Clock.System.now(),
        Clock.System.now() + Threat.THREAT_AGEING_WINDOW
    ),
) : ThreatRepository {

    private val threatsFlow = MutableStateFlow(listOf(initialThreat))

    var refreshCount: Int = 0
        private set
    var refreshFailure: Throwable? = null

    fun emit(value: Threat) {
        threatsFlow.value = listOf(value)
    }

    override fun observeThreat(): Flow<List<Threat>> = threatsFlow

    override fun getNearbyThreats(): List<Threat> {
        return threatsFlow.value
    }

    override suspend fun refreshThreat() {
        refreshCount += 1
        refreshFailure?.let { throw it }
    }
}
