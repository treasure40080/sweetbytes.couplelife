package com.sweetbytes.couplelife.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entries")
data class EntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Int,
    val type: EntryType,
    val category: String,
    val createdAt: Long
)

enum class EntryType { INCOME, EXPENSE }
