package com.zenlauncher.zenmode.coreapi

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_usage")
data class DailyUsageEntity(
    @PrimaryKey val date: String, // format: "yyyy-MM-dd"
    val screenTimeInMillis: Long
)
