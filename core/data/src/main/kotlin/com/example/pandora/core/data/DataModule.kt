package com.example.pandora.core.data

import com.example.pandora.core.domain.ThreatRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {
    @Binds
    @Singleton
    abstract fun bindThreatsLocalDataSource(implementation: InMemoryThreatsLocalDataSource): ThreatsLocalDataSource

    @Binds
    @Singleton
    abstract fun bindThreatRepository(implementation: DefaultThreatRepository): ThreatRepository

    companion object {
        @OptIn(ExperimentalTime::class)
        @Provides
        @Singleton
        fun provideClock(): Clock = Clock.System
    }
}
