package com.example.pandora.core.data

import com.example.pandora.core.model.Area
import com.example.pandora.core.model.Severity
import com.example.pandora.core.model.Threat
import com.example.pandora.core.model.ThreatsResult
import com.example.pandora.core.model.collectionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

internal interface ThreatsLocalDataSource {
    fun observeThreat(): Flow<ThreatsResult>

    fun getNearbyThreats(): List<Threat>

    suspend fun refreshThreat()
}

@Singleton
internal class InMemoryThreatsLocalDataSource
    @OptIn(ExperimentalTime::class)
    @Inject
    constructor(
        private val clock: Clock
    ) : ThreatsLocalDataSource {

    private var threats = emptyList<Threat>()

    private var threatIndex = 0
    private val internalFlow = MutableStateFlow(threats)

    @OptIn(ExperimentalTime::class)
    override fun observeThreat(): Flow<ThreatsResult> = internalFlow.map {
        if (it.isEmpty()) {
            ThreatsResult.Empty
        } else {
            val now = clock.now()
            ThreatsResult.Success(
                threats = it,
                status = it.collectionStatus(now),
            )
        }
    }

    override fun getNearbyThreats(): List<Threat> {
        return threats
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun refreshThreat() {
        val now = clock.now()

        val updatedThreats = if (threats.isEmpty()) {
            createSeededThreats(now)
        } else {
            threatIndex = (threatIndex + 1) % threats.size
            threats.mapIndexed { index, t ->
                if (index == threatIndex) {
                    t.copy(reportedAt = now, expiresAt = now + Threat.THREAT_AGEING_WINDOW)
                } else {
                    t
                }
            }
        }
        threats = updatedThreats
        internalFlow.value = updatedThreats
    }

    @OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
    private fun createSeededThreats(now: Instant): List<Threat> {
        return listOf(
            Threat(
                "cholera-abuja",
                "Cholera",
                Severity.HIGH,
                Area(9.0021987, 7.3450184),
                listOf("Make sure to only drink water from reliable sources"),
                now,
                now + Threat.THREAT_AGEING_WINDOW,
            ),
            Threat(
                "lassa-karu",
                "Lassa Fever",
                Severity.CRITICAL,
                Area(8.9947353, 7.5805829),
                listOf(
                    "Store food in sealed, rodent-proof containers",
                    "Keep your home clean and free of food waste to discourage rats",
                    "Avoid contact with rats and items contaminated by rodent urine or faeces",
                    "Cook food thoroughly before eating"
                ),
                now,
                now + Threat.THREAT_AGEING_WINDOW,
            ),
            Threat(
                "mpox-abuja",
                "mpox",
                Severity.LOW,
                Area(9.1172276, 7.3939031),
                listOf(
                    "Avoid close contact with anyone showing symptoms of smallpox",
                    "Avoid sharing clothing, bedding, or other items used by an infected person",
                    "Wash your hands frequently with soap and water",
                    "If you suspect exposure, contact a healthcare professional immediately"
                ),
                now,
                now + Threat.THREAT_AGEING_WINDOW,
            )
        )
    }
}
