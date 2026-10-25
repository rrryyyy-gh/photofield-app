package com.photofieldapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.photofieldapp.data.local.converter.DateTimeConverter
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Entity(tableName = "projects")
@Serializable
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val locationName: String = "",
    val locationLatitude: Double = 0.0,
    val locationLongitude: Double = 0.0,
    @TypeConverters(DateTimeConverter::class) val createdAt: Instant = Instant.now(),
    @TypeConverters(DateTimeConverter::class) val updatedAt: Instant = Instant.now()
) {
    companion object {
        fun create(
            name: String,
            description: String = "",
            locationName: String = "",
            locationLatitude: Double = 0.0,
            locationLongitude: Double = 0.0
        ): Project {
            val now = Instant.now()
            return Project(
                name = name,
                description = description,
                locationName = locationName,
                locationLatitude = locationLatitude,
                locationLongitude = locationLongitude,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    fun copyWithUpdated(name: String? = null, description: String? = null, locationName: String? = null, locationLatitude: Double? = null, locationLongitude: Double? = null): Project {
        return copy(
            name = name ?: this.name,
            description = description ?: this.description,
            locationName = locationName ?: this.locationName,
            locationLatitude = locationLatitude ?: this.locationLatitude,
            locationLongitude = locationLongitude ?: this.locationLongitude,
            updatedAt = Instant.now()
        )
    }
}