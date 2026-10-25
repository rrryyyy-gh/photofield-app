package com.photofieldapp.data.repository

import com.photofieldapp.data.local.dao.ActivityDao
import com.photofieldapp.data.local.entity.Activity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepositoryImpl @Inject constructor(
    private val activityDao: ActivityDao
) : ActivityRepository {

    override suspend fun createActivity(activity: Activity): Long {
        return activityDao.insert(activity)
    }

    override suspend fun updateActivity(activity: Activity) {
        activityDao.update(activity)
    }

    override suspend fun deleteActivity(activityId: Long) {
        activityDao.deleteById(activityId)
    }

    override suspend fun deleteActivitiesByProject(projectId: Long) {
        activityDao.deleteByProjectId(projectId)
    }

    override fun getActivitiesByProject(projectId: Long): Flow<List<Activity>> {
        return activityDao.getActivitiesByProject(projectId)
    }

    override suspend fun getActivityById(activityId: Long): Activity? {
        return activityDao.getActivityById(activityId)
    }

    override suspend fun updateSortOrder(activityId: Long, sortOrder: Int) {
        activityDao.updateSortOrder(activityId, sortOrder)
    }

    override suspend fun getNextSortOrder(projectId: Long): Int {
        val maxOrder = activityDao.getMaxSortOrder(projectId)
        return (maxOrder ?: -1) + 1
    }
}