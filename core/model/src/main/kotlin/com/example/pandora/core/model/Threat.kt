package com.example.pandora.core.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class Threat @OptIn(ExperimentalTime::class) constructor(
    val id: String,
    val name: String,
    val severity: Severity,
    val area: Area,
    val guidance: List<String>,
    val reportedAt: Instant,
    val expiresAt: Instant
)
