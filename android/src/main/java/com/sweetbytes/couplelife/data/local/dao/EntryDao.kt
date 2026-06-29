package com.sweetbytes.couplelife.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sweetbytes.couplelife.data.local.entity.EntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: EntryEntity)

    @Update
    suspend fun update(entry: EntryEntity)

    @Delete
    suspend fun delete(entry: EntryEntity)

    @Query("""
        SELECT * FROM entries
        WHERE strftime('%Y', createdAt / 1000, 'unixepoch') = :year
          AND strftime('%m', createdAt / 1000, 'unixepoch') = :month
        ORDER BY createdAt DESC
    """)
    fun getEntriesByMonth(year: String, month: String): Flow<List<EntryEntity>>
}
