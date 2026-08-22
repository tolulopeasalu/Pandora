package com.example.pandora.core.testing
import com.example.pandora.core.domain.ThreatRepository
import com.example.pandora.core.model.Area
import com.example.pandora.core.model.Severity
import com.example.pandora.core.model.Threat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
val now = Clock.System.now()

class FakeThreatRepository @OptIn(ExperimentalTime::class) constructor(

    initialThreat: Threat = Threat(
        "cholera-abuja",
        "Cholera",
        Severity.LOW,
        Area(9.0021987, 7.3450184),
        listOf("Make sure to only drink water from reliable sources"),
            now,
        now + 5.minutes
        ),
) : ThreatRepository {

    private val threat = MutableStateFlow(initialThreat)

    var refreshCount: Int = 0
        private set
    var refreshFailure: Throwable? = null

    fun emit(value: Threat) {
        threat.value = value
    }

    override fun observeThreat(): Flow<Threat> = threat

    override fun getNearbyThreats(): List<Threat> {
        return listOf(threat.value)
    }

    override suspend fun refreshThreat() {
        refreshCount += 1
        refreshFailure?.let { throw it }
    }
}
