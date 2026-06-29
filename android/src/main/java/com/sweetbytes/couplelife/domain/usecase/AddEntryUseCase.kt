package com.sweetbytes.couplelife.domain.usecase

import com.sweetbytes.couplelife.data.local.entity.EntryEntity
import com.sweetbytes.couplelife.domain.repository.EntryRepository
import javax.inject.Inject

class AddEntryUseCase @Inject constructor(
    private val repository: EntryRepository
) {
    suspend operator fun invoke(entry: EntryEntity) = repository.addEntry(entry)
}
