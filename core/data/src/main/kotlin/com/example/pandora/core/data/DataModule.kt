package com.example.pandora.core.data

import com.example.pandora.core.domain.ThreatRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {
    @Binds
    @Singleton
    abstract fun bindThreatsLocalDataSource(implementation: InMemoryThreatsLocalDataSource): ThreatsLocalDataSource

    @Binds
    @Singleton
    abstract fun bindThreatRepository(implementation: DefaultThreatRepository): ThreatRepository
}
