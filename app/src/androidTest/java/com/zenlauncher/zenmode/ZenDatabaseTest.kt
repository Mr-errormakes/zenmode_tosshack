package com.zenlauncher.zenmode

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zenlauncher.zenmode.coreapi.DailyUsageEntity
import com.zenlauncher.zenmode.coreapi.UsageDao
import com.zenlauncher.zenmode.coreapi.ZenDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ZenDatabaseTest {
    private lateinit var db: ZenDatabase
    private lateinit var usageDao: UsageDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ZenDatabase::class.java).build()
        usageDao = db.usageDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeAndReadUsage() = runBlocking {
        val entity = DailyUsageEntity("2026-07-22", 5000L)
        usageDao.insert(entity)
        val result = usageDao.getScreenTimeForDate("2026-07-22")
        assertEquals(5000L, result?.screenTimeInMillis)
    }

    @Test
    fun clearAllData() = runBlocking {
        usageDao.insert(DailyUsageEntity("2026-07-21", 1000L))
        usageDao.insert(DailyUsageEntity("2026-07-22", 2000L))
        usageDao.clearAll()
        val result = usageDao.getScreenTimeForDate("2026-07-22")
        assertNull(result)
    }
}
