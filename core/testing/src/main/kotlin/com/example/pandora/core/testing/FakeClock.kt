package com.example.pandora.core.testing

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class FakeClock(initialTime: Instant = Instant.DISTANT_PAST) : Clock {
    private var _now: Instant = initialTime

    override fun now(): Instant = _now

    fun advanceBy(duration: Duration) {
        _now += duration
    }

    fun setTime(time: Instant) {
        _now = time
    }
}
