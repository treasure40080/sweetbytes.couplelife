package com.sweetbytes.couplelife.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sweetbytes.couplelife.data.local.converter.EntryTypeConverter
import com.sweetbytes.couplelife.data.local.dao.EntryDao
import com.sweetbytes.couplelife.data.local.entity.EntryEntity

@Database(entities = [EntryEntity::class], version = 1, exportSchema = false)
@TypeConverters(EntryTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao
}
