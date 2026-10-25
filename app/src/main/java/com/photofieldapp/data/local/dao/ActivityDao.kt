package com.photofieldapp.data.local.dao

import androidx.room.*
import com.photofieldapp.data.local.entity.Activity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: Activity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(activities: List<Activity>)

    @Update
    suspend fun update(activity: Activity): Int

    @Delete
    suspend fun delete(activity: Activity): Int

    @Query("DELETE FROM activities WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM activities WHERE projectId = :projectId")
    suspend fun deleteByProjectId(projectId: Long): Int

    @Query("SELECT * FROM activities WHERE projectId = :projectId ORDER BY sortOrder ASC, createdAt ASC")
    fun getActivitiesByProject(projectId: Long): Flow<List<Activity>>

    @Query("SELECT * FROM activities WHERE projectId = :projectId ORDER BY sortOrder ASC, createdAt ASC")
    suspend fun getActivitiesByProjectOnce(projectId: Long): List<Activity>

    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getActivityById(id: Long): Activity?

    @Query("SELECT * FROM activities WHERE id = :id")
    fun getActivityByIdFlow(id: Long): Flow<Activity?>

    @Query("SELECT COUNT(*) FROM activities WHERE projectId = :projectId")
    suspend fun getActivityCount(projectId: Long): Int

    @Query("SELECT MAX(sortOrder) FROM activities WHERE projectId = :projectId")
    suspend fun getMaxSortOrder(projectId: Long): Int?

    @Query("UPDATE activities SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int): Int

    @Query("UPDATE activities SET updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTimestamp(id: Long, updatedAt: Long)
}