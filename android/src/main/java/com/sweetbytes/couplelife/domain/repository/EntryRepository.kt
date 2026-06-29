package com.sweetbytes.couplelife.domain.repository

import com.sweetbytes.couplelife.data.local.entity.EntryEntity
import kotlinx.coroutines.flow.Flow

interface EntryRepository {
    suspend fun addEntry(entry: EntryEntity)
    fun getEntriesByMonth(year: String, month: String): Flow<List<EntryEntity>>
    suspend fun updateEntry(entry: EntryEntity)
    suspend fun deleteEntry(entry: EntryEntity)
}
