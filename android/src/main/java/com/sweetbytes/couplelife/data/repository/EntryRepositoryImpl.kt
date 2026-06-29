package com.sweetbytes.couplelife.data.repository

import com.sweetbytes.couplelife.data.local.dao.EntryDao
import com.sweetbytes.couplelife.data.local.entity.EntryEntity
import com.sweetbytes.couplelife.domain.repository.EntryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EntryRepositoryImpl @Inject constructor(
    private val dao: EntryDao
) : EntryRepository {
    override suspend fun addEntry(entry: EntryEntity) = dao.insert(entry)
    override fun getEntriesByMonth(year: String, month: String): Flow<List<EntryEntity>> =
        dao.getEntriesByMonth(year, month)
    override suspend fun updateEntry(entry: EntryEntity) = dao.update(entry)
    override suspend fun deleteEntry(entry: EntryEntity) = dao.delete(entry)
}
