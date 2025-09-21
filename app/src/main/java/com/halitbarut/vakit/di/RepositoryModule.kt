package com.halitbarut.vakit.di

import com.halitbarut.vakit.data.repository.PrayerRepositoryImpl
import com.halitbarut.vakit.domain.repository.PrayerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindPrayerRepository(impl: PrayerRepositoryImpl): PrayerRepository
}
