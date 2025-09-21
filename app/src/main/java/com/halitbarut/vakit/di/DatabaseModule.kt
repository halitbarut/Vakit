package com.halitbarut.vakit.di

import android.content.Context
import androidx.room.Room
import com.halitbarut.vakit.data.local.dao.DailyLogDao
import com.halitbarut.vakit.data.local.dao.PrayerDao
import com.halitbarut.vakit.data.local.database.VakitDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VakitDatabase =
        Room.databaseBuilder(
            context,
            VakitDatabase::class.java,
            "vakit.db",
        ).addMigrations(
            VakitDatabase.MIGRATION_1_2,
            VakitDatabase.MIGRATION_2_3,
        ).build()

    @Provides
    fun providePrayerDao(database: VakitDatabase): PrayerDao = database.prayerDao()

    @Provides
    fun provideDailyLogDao(database: VakitDatabase): DailyLogDao = database.dailyLogDao()
}
