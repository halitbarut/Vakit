package com.halitbarut.vakit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.halitbarut.vakit.data.local.entity.PrayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerDao {
    @Query("SELECT * FROM prayer_stats WHERE id = :id LIMIT 1")
    fun observePrayerStats(id: Int = PrayerEntity.SINGLETON_ID): Flow<PrayerEntity?>

    @Query("SELECT * FROM prayer_stats WHERE id = :id LIMIT 1")
    suspend fun getPrayerStats(id: Int = PrayerEntity.SINGLETON_ID): PrayerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(prayerEntity: PrayerEntity)

    @Query("DELETE FROM prayer_stats")
    suspend fun clearAll()
}
