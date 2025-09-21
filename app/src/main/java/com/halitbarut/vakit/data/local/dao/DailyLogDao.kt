package com.halitbarut.vakit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.halitbarut.vakit.data.local.entity.DailyLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: DailyLogEntity)

    @Query("SELECT * FROM daily_logs WHERE dateEpochDay = :date LIMIT 1")
    suspend fun getByDate(date: Long): DailyLogEntity?

    @Query("SELECT * FROM daily_logs WHERE dateEpochDay BETWEEN :startDate AND :endDate ORDER BY dateEpochDay ASC")
    fun getLogsForDateRange(startDate: Long, endDate: Long): Flow<List<DailyLogEntity>>

    @Query("DELETE FROM daily_logs")
    suspend fun clearAll()
}
