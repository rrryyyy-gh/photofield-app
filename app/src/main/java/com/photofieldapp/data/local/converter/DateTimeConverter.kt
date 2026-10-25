package com.photofieldapp.data.local.converter

import androidx.room.TypeConverter
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormatter

class DateTimeConverter {

    @TypeConverter
    fun fromInstant(value: Instant?): Long? {
        return value?.epochMilliseconds
    }

    @TypeConverter
    fun toInstant(value: Long?): Instant? {
        return value?.let { Instant.fromEpochMilliseconds(it) }
    }
}

class ListConverter {

    @TypeConverter
    fun fromList(list: List<Long>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    fun toList(value: String?): List<Long>? {
        return value?.split(",")?.map { it.toLong() }?.filter { it > 0 }
    }
}