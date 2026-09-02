package com.example.pandora.core.testing
import com.example.pandora.core.domain.ThreatRepository
import com.example.pandora.core.model.Area
import com.example.pandora.core.model.Severity
import com.example.pandora.core.model.Threat
import com.example.pandora.core.model.ThreatStatus
import com.example.pandora.core.model.ThreatsResult
import com.example.pandora.core.model.collectionStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class FakeThreatRepository constructor(
    initialThreats: List<Threat> = emptyList(),
    private val clock: Clock = Clock.System
) : ThreatRepository {

    private val internalFlow = MutableStateFlow(initialThreats)

    var refreshCount: Int = 0
        private set
    var refreshFailure: Throwable? = null



    fun emit(values: List<Threat>) {
        internalFlow.value = values
    }

    @OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
    override fun observeThreat(): Flow<ThreatsResult> = internalFlow.flatMapLatest { threats ->
        if (threats.isEmpty()) {
            flowOf<ThreatsResult>(ThreatsResult.Empty)
        } else {
            flow {
                while (true) {
                    val now = clock.now()
                    val status = threats.collectionStatus(now)
                    emit(ThreatsResult.Success(threats, status))

                    if (status == ThreatStatus.STALE) break

                    val earliestExpiry = threats.minOf { it.expiresAt }
                    val delayMillis = (earliestExpiry - now).inWholeMilliseconds
                    if (delayMillis <= 0) continue

                    delay((delayMillis + 1).milliseconds)
                }
            }
        }
    }

    override fun getNearbyThreats(): List<Threat> {
        return internalFlow.value
    }

    override suspend fun refreshThreat() {
        refreshCount += 1
        refreshFailure?.let { throw it }
    }
}
