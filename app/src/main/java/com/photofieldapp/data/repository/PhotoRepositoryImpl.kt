package com.photofieldapp.data.repository

import com.photofieldapp.data.local.dao.PhotoDao
import com.photofieldapp.data.local.entity.Photo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepositoryImpl @Inject constructor(
    private val photoDao: PhotoDao
) : PhotoRepository {

    override suspend fun createPhoto(photo: Photo): Long {
        return photoDao.insert(photo)
    }

    override suspend fun updatePhoto(photo: Photo) {
        photoDao.update(photo)
    }

    override suspend fun deletePhoto(photoId: Long) {
        photoDao.deleteById(photoId)
    }

    override suspend fun deletePhotosByActivity(activityId: Long) {
        photoDao.deleteByActivityId(activityId)
    }

    override fun getPhotosByActivity(activityId: Long): Flow<List<Photo>> {
        return photoDao.getPhotosByActivity(activityId)
    }

    override suspend fun getPhotoById(photoId: Long): Photo? {
        return photoDao.getPhotoById(photoId)
    }

    override suspend fun updateSortOrder(photoId: Long, sortOrder: Int) {
        photoDao.updateSortOrder(photoId, sortOrder)
    }

    override suspend fun getNextSortOrder(activityId: Long): Int {
        val maxOrder = photoDao.getMaxSortOrder(activityId)
        return (maxOrder ?: -1) + 1
    }

    override fun getPhotosWithLocation(activityId: Long): Flow<List<Photo>> {
        return photoDao.getPhotosWithLocationByActivity(activityId)
    }

    override suspend fun getPhotosWithLocationOnce(activityId: Long): List<Photo> {
        return photoDao.getPhotosWithLocationByActivityOnce(activityId)
    }
}