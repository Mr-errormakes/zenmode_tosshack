package com.zenlauncher.zenmode.coreapi

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DailyUsageEntity::class], version = 1, exportSchema = false)
abstract class ZenDatabase : RoomDatabase() {
    abstract fun usageDao(): UsageDao

    companion object {
        @Volatile
        private var INSTANCE: ZenDatabase? = null

        fun getDatabase(context: Context): ZenDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ZenDatabase::class.java,
                    "zen_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
