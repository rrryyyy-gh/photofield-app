package com.photofieldapp.data.repository

import com.photofieldapp.data.local.dao.ActivityDao
import com.photofieldapp.data.local.dao.PhotoDao
import com.photofieldapp.data.local.dao.ProjectDao
import com.photofieldapp.data.local.dao.PdfTemplateDao
import com.photofieldapp.data.local.entity.Activity
import com.photofieldapp.data.local.entity.Photo
import com.photofieldapp.data.local.entity.Project
import com.photofieldapp.data.local.entity.PdfTemplate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

interface ProjectRepository {
    suspend fun createProject(project: Project): Long
    suspend fun updateProject(project: Project)
    suspend fun deleteProject(projectId: Long)
    fun getAllProjects(): Flow<List<Project>>
    suspend fun getProjectById(projectId: Long): Project?
}

interface ActivityRepository {
    suspend fun createActivity(activity: Activity): Long
    suspend fun updateActivity(activity: Activity)
    suspend fun deleteActivity(activityId: Long)
    suspend fun deleteActivitiesByProject(projectId: Long)
    fun getActivitiesByProject(projectId: Long): Flow<List<Activity>>
    suspend fun getActivityById(activityId: Long): Activity?
    suspend fun updateSortOrder(activityId: Long, sortOrder: Int)
    suspend fun getNextSortOrder(projectId: Long): Int
}

interface PhotoRepository {
    suspend fun createPhoto(photo: Photo): Long
    suspend fun updatePhoto(photo: Photo)
    suspend fun deletePhoto(photoId: Long)
    suspend fun deletePhotosByActivity(activityId: Long)
    fun getPhotosByActivity(activityId: Long): Flow<List<Photo>>
    suspend fun getPhotoById(photoId: Long): Photo?
    suspend fun updateSortOrder(photoId: Long, sortOrder: Int)
    suspend fun getNextSortOrder(activityId: Long): Int
    fun getPhotosWithLocation(activityId: Long): Flow<List<Photo>>
    suspend fun getPhotosWithLocationOnce(activityId: Long): List<Photo>
}

interface PdfTemplateRepository {
    suspend fun createTemplate(template: PdfTemplate): Long
    suspend fun updateTemplate(template: PdfTemplate)
    suspend fun deleteTemplate(templateId: Long)
    fun getAllTemplates(): Flow<List<PdfTemplate>>
    suspend fun getDefaultTemplate(): PdfTemplate?
    suspend fun setDefaultTemplate(templateId: Long)
}