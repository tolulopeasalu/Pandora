package com.example.pandora.core.model

import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class Threat constructor(
    val id: String,
    val name: String,
    val severity: Severity,
    val area: Area,
    val guidance: List<String>,
    val reportedAt: Instant,
    val expiresAt: Instant
) {
    fun status(now: Instant): ThreatStatus =
        if (now < expiresAt) ThreatStatus.FRESH else ThreatStatus.STALE

    companion object {
        val THREAT_AGEING_WINDOW = 15.minutes
    }
}

@OptIn(ExperimentalTime::class)
fun List<Threat>.collectionStatus(now: Instant): ThreatStatus =
    if (any { it.status(now) == ThreatStatus.STALE }) ThreatStatus.STALE else ThreatStatus.FRESH

enum class ThreatStatus {
    FRESH, STALE
}
