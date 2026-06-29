package com.sweetbytes.couplelife.data.local.converter

import androidx.room.TypeConverter
import com.sweetbytes.couplelife.data.local.entity.EntryType

class EntryTypeConverter {
    @TypeConverter
    fun fromEntryType(type: EntryType): String = type.name

    @TypeConverter
    fun toEntryType(value: String): EntryType = EntryType.valueOf(value)
}
