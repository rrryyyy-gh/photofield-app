package com.photofieldapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.photofieldapp.data.local.converter.DateTimeConverter
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Entity(
    tableName = "activities",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
@Serializable
data class Activity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val name: String,
    val description: String = "",
    val locationName: String = "",
    val locationLatitude: Double = 0.0,
    val locationLongitude: Double = 0.0,
    val sortOrder: Int = 0,
    @TypeConverters(DateTimeConverter::class) val createdAt: Instant = Instant.now(),
    @TypeConverters(DateTimeConverter::class) val updatedAt: Instant = Instant.now()
) {
    companion object {
        fun create(
            projectId: Long,
            name: String,
            description: String = "",
            locationName: String = "",
            locationLatitude: Double = 0.0,
            locationLongitude: Double = 0.0,
            sortOrder: Int = 0
        ): Activity {
            val now = Instant.now()
            return Activity(
                projectId = projectId,
                name = name,
                description = description,
                locationName = locationName,
                locationLatitude = locationLatitude,
                locationLongitude = locationLongitude,
                sortOrder = sortOrder,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    fun copyWithUpdated(
        name: String? = null,
        description: String? = null,
        locationName: String? = null,
        locationLatitude: Double? = null,
        locationLongitude: Double? = null,
        sortOrder: Int? = null
    ): Activity {
        return copy(
            name = name ?: this.name,
            description = description ?: this.description,
            locationName = locationName ?: this.locationName,
            locationLatitude = locationLatitude ?: this.locationLatitude,
            locationLongitude = locationLongitude ?: this.locationLongitude,
            sortOrder = sortOrder ?: this.sortOrder,
            updatedAt = Instant.now()
        )
    }
}