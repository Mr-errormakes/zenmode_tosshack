package com.zenlauncher.zenmode.coreapi

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DailyUsageEntity)

    @Query("SELECT * FROM daily_usage WHERE date = :date LIMIT 1")
    suspend fun getScreenTimeForDate(date: String): DailyUsageEntity?

    @Query("SELECT * FROM daily_usage ORDER BY date ASC")
    suspend fun queryDailyStats(): List<DailyUsageEntity>

    @Query("DELETE FROM daily_usage WHERE date < :cutoffDate")
    suspend fun pruneStaleEntries(cutoffDate: String)

    @Query("DELETE FROM daily_usage")
    suspend fun clearAll()
}
