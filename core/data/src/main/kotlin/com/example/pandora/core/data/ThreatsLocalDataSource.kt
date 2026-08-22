package com.example.pandora.core.data

import com.example.pandora.core.model.Area
import com.example.pandora.core.model.Severity
import com.example.pandora.core.model.Threat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

internal interface ThreatsLocalDataSource {
    fun observeThreat(): Flow<Threat>

    fun getNearbyThreats(): List<Threat>

    suspend fun refreshThreat()
}

@Singleton
internal class InMemoryThreatsLocalDataSource
    @Inject
    constructor() : ThreatsLocalDataSource {

    @OptIn(ExperimentalTime::class)
    val reportedTime = Clock.System.now()
    @OptIn(ExperimentalTime::class)
    val expiresAt = reportedTime + 5.minutes
    @OptIn(ExperimentalTime::class)
    val expiryPeriod = reportedTime + 15.minutes
    @OptIn(ExperimentalTime::class)
    val expiryTime = reportedTime + 30.minutes

        @OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
        private val threats =
            listOf(
                Threat(
                     "cholera-abuja",
                    "Cholera",
                    Severity.LOW,
                    Area(9.0021987, 7.3450184),
                    listOf("Make sure to only drink water from reliable sources"),
                reportedTime,
                expiresAt,
            ),
                    Threat(
                    "lassa-abuja",
                "Lassa Fever",
                Severity.LOW,
                Area(9.0021987, 7.3450184),
                listOf("Make sure to only drink water from reliable sources"),
                reportedTime,
                expiryPeriod,
            ),
                Threat(
                    "smallpox-abuja",
                    "Smallpox",
                    Severity.LOW,
                    Area(9.0021987, 7.3450184),
                    listOf("Make sure to only drink water from reliable sources"),
                    reportedTime,
                    expiryTime,
                )
            )
        private var threatIndex = 0
        @OptIn(ExperimentalTime::class)
        private val threat = MutableStateFlow(threats[threatIndex])

        override fun observeThreat(): Flow<Threat> = threat
    override fun getNearbyThreats(): List<Threat> {
        return listOf(threat.value)
    }

    @OptIn(ExperimentalTime::class)
        override suspend fun refreshThreat() {
            threatIndex = (threatIndex + 1) % threats.size
            threat.value = threats[threatIndex]
        }
    }
