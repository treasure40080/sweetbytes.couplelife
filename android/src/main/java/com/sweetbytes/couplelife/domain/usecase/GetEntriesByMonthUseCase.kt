package com.sweetbytes.couplelife.domain.usecase

import com.sweetbytes.couplelife.data.local.entity.EntryEntity
import com.sweetbytes.couplelife.domain.repository.EntryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEntriesByMonthUseCase @Inject constructor(
    private val repository: EntryRepository
) {
    operator fun invoke(year: String, month: String): Flow<List<EntryEntity>> =
        repository.getEntriesByMonth(year, month)
}
