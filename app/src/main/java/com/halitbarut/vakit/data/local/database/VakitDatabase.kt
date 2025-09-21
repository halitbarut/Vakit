package com.halitbarut.vakit.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.halitbarut.vakit.data.local.dao.DailyLogDao
import com.halitbarut.vakit.data.local.dao.PrayerDao
import com.halitbarut.vakit.data.local.entity.DailyLogEntity
import com.halitbarut.vakit.data.local.entity.PrayerEntity

@Database(
    entities = [PrayerEntity::class, DailyLogEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class VakitDatabase : RoomDatabase() {
    abstract fun prayerDao(): PrayerDao
    abstract fun dailyLogDao(): DailyLogDao

    companion object {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE prayer_stats ADD COLUMN fajr_last_update_millis INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE prayer_stats ADD COLUMN dhuhr_last_update_millis INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE prayer_stats ADD COLUMN asr_last_update_millis INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE prayer_stats ADD COLUMN maghrib_last_update_millis INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE prayer_stats ADD COLUMN isha_last_update_millis INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE prayer_stats ADD COLUMN witr_last_update_millis INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS daily_logs (" +
                        "dateEpochDay INTEGER NOT NULL PRIMARY KEY, " +
                        "fajrCompleted INTEGER NOT NULL DEFAULT 0, " +
                        "dhuhrCompleted INTEGER NOT NULL DEFAULT 0, " +
                        "asrCompleted INTEGER NOT NULL DEFAULT 0, " +
                        "maghribCompleted INTEGER NOT NULL DEFAULT 0, " +
                        "ishaCompleted INTEGER NOT NULL DEFAULT 0, " +
                        "witrCompleted INTEGER NOT NULL DEFAULT 0" +
                        ")",
                )
            }
        }
    }
}
