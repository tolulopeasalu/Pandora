package com.example.pandora.core.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class ThreatTest {

    private val now = Instant.fromEpochMilliseconds(1000000)
    private val freshThreat = Threat(
        id = "1",
        name = "Fresh",
        severity = Severity.LOW,
        area = Area(0.0, 0.0),
        guidance = emptyList(),
        reportedAt = now,
        expiresAt = now + 5.minutes
    )

    private val staleThreat = freshThreat.copy(
        id = "2",
        name = "Stale",
        expiresAt = now - 1.seconds
    )

    @Test
    fun `threat is fresh before expiry`() {
        assertThat(freshThreat.status(now)).isEqualTo(ThreatStatus.FRESH)
    }

    @Test
    fun `threat is stale after expiry`() {
        assertThat(staleThreat.status(now)).isEqualTo(ThreatStatus.STALE)
    }

    @Test
    fun `collection is fresh when all threats are fresh`() {
        val collection = listOf(freshThreat, freshThreat.copy(id = "3"))
        assertThat(collection.collectionStatus(now)).isEqualTo(ThreatStatus.FRESH)
    }

    @Test
    fun `collection is stale if any threat is stale`() {
        val collection = listOf(freshThreat, staleThreat)
        assertThat(collection.collectionStatus(now)).isEqualTo(ThreatStatus.STALE)
    }

    @Test
    fun `empty collection is fresh`() {
        assertThat(emptyList<Threat>().collectionStatus(now)).isEqualTo(ThreatStatus.FRESH)
    }
}
