package com.example.pandora.core.data

import com.example.pandora.core.model.Area
import com.example.pandora.core.model.Severity
import com.example.pandora.core.model.Threat
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

class DefaultThreatRepositoryTest {
    private val dataSource = FakeThreatsLocalDataSource()
    private val repository = DefaultThreatRepository(dataSource)

    @Test
    fun `observe threat delegates to local data source`() =
        runTest {
            assertThat(repository.observeThreat().first().name).isEqualTo("Cholera")
        }

    @Test
    fun `refresh threat updates observed value`() =
        runTest {
            repository.refreshThreat()

            assertThat(repository.observeThreat().first().name).isEqualTo("Refreshed")
            assertThat(dataSource.refreshCount).isEqualTo(1)
        }
}

private class FakeThreatsLocalDataSource : ThreatsLocalDataSource {
    @OptIn(ExperimentalTime::class)
    val reportedTime = Clock.System.now()
    @OptIn(ExperimentalTime::class)
    private val threat = MutableStateFlow(Threat(
        "cholera-abuja",
        "Cholera",
        Severity.LOW,
        Area(9.0021987, 7.3450184),
        listOf("Make sure to only drink water from reliable sources"),
        reportedTime,
         expiresAt =  reportedTime + 5.minutes
        ))


    var refreshCount = 0
        private set

    override fun observeThreat(): Flow<Threat> = threat
    override fun getNearbyThreats(): List<Threat> {
        return listOf(threat.value)
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun refreshThreat() {
        refreshCount += 1
        threat.value = Threat(
            "cholera-abuja-refresh",
            "Cholera",
            Severity.LOW,
            Area(9.0021987, 7.3450184),
            listOf("Make sure to only drink water from reliable sources"),
            reportedTime,
            reportedTime + 5.minutes
        )
    }
}
