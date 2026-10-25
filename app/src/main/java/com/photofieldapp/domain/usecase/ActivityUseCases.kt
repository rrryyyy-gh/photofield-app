package com.photofieldapp.domain.usecase

import com.photofieldapp.data.repository.ActivityRepository
import com.photofieldapp.domain.model.Activity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActivitiesUseCase @Inject constructor(
    private val repository: ActivityRepository
) {
    operator fun invoke(projectId: Long): Flow<List<Activity>> = repository.getActivitiesByProject(projectId)
}

class GetActivityUseCase @Inject constructor(
    private val repository: ActivityRepository
) {
    operator fun invoke(activityId: Long): Activity? = repository.getActivityById(activityId)
}

class CreateActivityUseCase @Inject constructor(
    private val repository: ActivityRepository
) {
    operator fun invoke(activity: Activity): Long = repository.createActivity(activity)
}

class UpdateActivityUseCase @Inject constructor(
    private val repository: ActivityRepository
) {
    operator fun invoke(activity: Activity) = repository.updateActivity(activity)
}

class DeleteActivityUseCase @Inject constructor(
    private val repository: ActivityRepository
) {
    operator fun invoke(activityId: Long) = repository.deleteActivity(activityId)
}

class UpdateActivitySortOrderUseCase @Inject constructor(
    private val repository: ActivityRepository
) {
    operator fun invoke(activityId: Long, sortOrder: Int) = repository.updateSortOrder(activityId, sortOrder)
}

class GetNextActivitySortOrderUseCase @Inject constructor(
    private val repository: ActivityRepository
) {
    operator fun invoke(projectId: Long): Int = repository.getNextSortOrder(projectId)
}