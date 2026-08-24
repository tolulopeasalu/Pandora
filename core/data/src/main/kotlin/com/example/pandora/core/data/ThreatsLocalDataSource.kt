package com.example.pandora.core.data

import com.example.pandora.core.model.Area
import com.example.pandora.core.model.Severity
import com.example.pandora.core.model.Threat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

internal interface ThreatsLocalDataSource {
    fun observeThreat(): Flow<List<Threat>>

    fun getNearbyThreats(): List<Threat>

    suspend fun refreshThreat()
}

@Singleton
internal class InMemoryThreatsLocalDataSource
    @Inject
    constructor() : ThreatsLocalDataSource {

    @OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
    private var threats = run {
        val now = Clock.System.now()
        listOf(
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

    private var threatIndex = 0
    private val threatsFlow = MutableStateFlow(threats)

    override fun observeThreat(): Flow<List<Threat>> = threatsFlow

    override fun getNearbyThreats(): List<Threat> {
        return threats
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun refreshThreat() {
        val now = Clock.System.now()
        threatIndex = (threatIndex + 1) % threats.size

        val updatedThreats = threats.mapIndexed { index, t ->
            if (index == threatIndex) {
                t.copy(reportedAt = now, expiresAt = now + Threat.THREAT_AGEING_WINDOW)
            } else {
                t
            }
        }
        threats = updatedThreats
        threatsFlow.value = updatedThreats
    }
}
