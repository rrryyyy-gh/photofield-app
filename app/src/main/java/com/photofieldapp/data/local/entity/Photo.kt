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
    tableName = "photos",
    foreignKeys = [
        ForeignKey(
            entity = Activity::class,
            parentColumns = ["id"],
            childColumns = ["activityId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("activityId")]
)
@Serializable
data class Photo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val activityId: Long,
    val filePath: String, // Relative path to file in app-specific storage
    val fileName: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val accuracy: Float = 0f,
    val bearing: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(), // Photo taken time
    val width: Int = 0,
    val height: Int = 0,
    val orientation: Int = 0,
    val sortOrder: Int = 0,
    val isSynced: Boolean = false,
    @TypeConverters(DateTimeConverter::class) val createdAt: Instant = Instant.now(),
    @TypeConverters(DateTimeConverter::class) val updatedAt: Instant = Instant.now()
) {
    companion object {
        fun create(
            activityId: Long,
            filePath: String,
            fileName: String,
            latitude: Double = 0.0,
            longitude: Double = 0.0,
            altitude: Double = 0.0,
            accuracy: Float = 0f,
            bearing: Float = 0f,
            timestamp: Long = System.currentTimeMillis(),
            width: Int = 0,
            height: Int = 0,
            orientation: Int = 0,
            sortOrder: Int = 0
        ): Photo {
            val now = Instant.now()
            return Photo(
                activityId = activityId,
                filePath = filePath,
                fileName = fileName,
                latitude = latitude,
                longitude = longitude,
                altitude = altitude,
                accuracy = accuracy,
                bearing = bearing,
                timestamp = timestamp,
                width = width,
                height = height,
                orientation = orientation,
                sortOrder = sortOrder,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    fun copyWithUpdated(
        filePath: String? = null,
        fileName: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        altitude: Double? = null,
        accuracy: Float? = null,
        bearing: Float? = null,
        timestamp: Long? = null,
        width: Int? = null,
        height: Int? = null,
        orientation: Int? = null,
        sortOrder: Int? = null,
        isSynced: Boolean? = null
    ): Photo {
        return copy(
            filePath = filePath ?: this.filePath,
            fileName = fileName ?: this.fileName,
            latitude = latitude ?: this.latitude,
            longitude = longitude ?: this.longitude,
            altitude = altitude ?: this.altitude,
            accuracy = accuracy ?: this.accuracy,
            bearing = bearing ?: this.bearing,
            timestamp = timestamp ?: this.timestamp,
            width = width ?: this.width,
            height = height ?: this.height,
            orientation = orientation ?: this.orientation,
            sortOrder = sortOrder ?: this.sortOrder,
            isSynced = isSynced ?: this.isSynced,
            updatedAt = Instant.now()
        )
    }

    val hasLocation: Boolean
        get() = latitude != 0.0 && longitude != 0.0
}