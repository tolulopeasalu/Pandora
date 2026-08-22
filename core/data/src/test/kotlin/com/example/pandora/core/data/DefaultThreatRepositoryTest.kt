package com.example.pandora.core.data

import com.example.pandora.core.model.Threat
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultThreatRepositoryTest {
    private val dataSource = FakeGreetingLocalDataSource()
    private val repository = DefaultThreatRepository(dataSource)

    @Test
    fun `observe greeting delegates to local data source`() =
        runTest {
            assertThat(repository.observeGreeting().first().message).isEqualTo("Initial")
        }

    @Test
    fun `refresh greeting updates observed value`() =
        runTest {
            repository.refreshGreeting()

            assertThat(repository.observeGreeting().first().message).isEqualTo("Refreshed")
            assertThat(dataSource.refreshCount).isEqualTo(1)
        }
}

private class FakeGreetingLocalDataSource : ThreatsLocalDataSource {
    private val threat = MutableStateFlow(Threat("Initial"))
    var refreshCount = 0
        private set

    override fun observeThreat(): Flow<Threat> = threat

    override suspend fun refreshThreat() {
        refreshCount += 1
        threat.value = Threat("Refreshed")
    }
}
