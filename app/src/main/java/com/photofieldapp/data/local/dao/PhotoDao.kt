package com.photofieldapp.data.local.dao

import androidx.room.*
import com.photofieldapp.data.local.entity.Photo
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: Photo): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(photos: List<Photo>)

    @Update
    suspend fun update(photo: Photo): Int

    @Delete
    suspend fun delete(photo: Photo): Int

    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM photos WHERE activityId = :activityId")
    suspend fun deleteByActivityId(activityId: Long): Int

    @Query("SELECT * FROM photos WHERE activityId = :activityId ORDER BY sortOrder ASC, timestamp ASC")
    fun getPhotosByActivity(activityId: Long): Flow<List<Photo>>

    @Query("SELECT * FROM photos WHERE activityId = :activityId ORDER BY sortOrder ASC, timestamp ASC")
    suspend fun getPhotosByActivityOnce(activityId: Long): List<Photo>

    @Query("SELECT * FROM photos WHERE id = :id")
    suspend fun getPhotoById(id: Long): Photo?

    @Query("SELECT * FROM photos WHERE id = :id")
    fun getPhotoByIdFlow(id: Long): Flow<Photo?>

    @Query("SELECT COUNT(*) FROM photos WHERE activityId = :activityId")
    suspend fun getPhotoCount(activityId: Long): Int

    @Query("SELECT MAX(sortOrder) FROM photos WHERE activityId = :activityId")
    suspend fun getMaxSortOrder(activityId: Long): Int?

    @Query("UPDATE photos SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int): Int

    @Query("UPDATE photos SET isSynced = :isSynced WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, isSynced: Boolean): Int

    @Query("SELECT * FROM photos WHERE activityId = :activityId AND latitude != 0.0 AND longitude != 0.0 ORDER BY timestamp ASC")
    fun getPhotosWithLocationByActivity(activityId: Long): Flow<List<Photo>>

    @Query("SELECT * FROM photos WHERE activityId = :activityId AND latitude != 0.0 AND longitude != 0.0 ORDER BY timestamp ASC")
    suspend fun getPhotosWithLocationByActivityOnce(activityId: Long): List<Photo>

    @Query("UPDATE photos SET updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTimestamp(id: Long, updatedAt: Long)
}