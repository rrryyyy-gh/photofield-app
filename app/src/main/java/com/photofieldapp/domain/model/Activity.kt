package com.photofieldapp.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Activity(
    val id: Long = 0,
    val projectId: Long,
    val name: String,
    val description: String = "",
    val locationName: String = "",
    val locationLatitude: Double = 0.0,
    val locationLongitude: Double = 0.0,
    val sortOrder: Int = 0,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
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