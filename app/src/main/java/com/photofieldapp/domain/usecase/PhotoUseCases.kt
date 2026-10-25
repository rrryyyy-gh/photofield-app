package com.photofieldapp.domain.usecase

import com.photofieldapp.data.repository.PhotoRepository
import com.photofieldapp.domain.model.Photo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPhotosUseCase @Inject constructor(
    private val repository: PhotoRepository
) {
    operator fun invoke(activityId: Long): Flow<List<Photo>> = repository.getPhotosByActivity(activityId)
}

class GetPhotoUseCase @Inject constructor(
    private val repository: PhotoRepository
) {
    operator fun invoke(photoId: Long): Photo? = repository.getPhotoById(photoId)
}

class CreatePhotoUseCase @Inject constructor(
    private val repository: PhotoRepository
) {
    operator fun invoke(photo: Photo): Long = repository.createPhoto(photo)
}

class UpdatePhotoUseCase @Inject constructor(
    private val repository: PhotoRepository
) {
    operator fun invoke(photo: Photo) = repository.updatePhoto(photo)
}

class DeletePhotoUseCase @Inject constructor(
    private val repository: PhotoRepository
) {
    operator fun invoke(photoId: Long) = repository.deletePhoto(photoId)
}

class DeletePhotosByActivityUseCase @Inject constructor(
    private val repository: PhotoRepository
) {
    operator fun invoke(activityId: Long) = repository.deletePhotosByActivity(activityId)
}

class UpdatePhotoSortOrderUseCase @Inject constructor(
    private val repository: PhotoRepository
) {
    operator fun invoke(photoId: Long, sortOrder: Int) = repository.updateSortOrder(photoId, sortOrder)
}

class GetNextPhotoSortOrderUseCase @Inject constructor(
    private val repository: PhotoRepository
) {
    operator fun invoke(activityId: Long): Int = repository.getNextSortOrder(activityId)
}

class GetPhotosWithLocationUseCase @Inject constructor(
    private val repository: PhotoRepository
) {
    operator fun invoke(activityId: Long): Flow<List<Photo>> = repository.getPhotosWithLocation(activityId)
    
    fun invokeOnce(activityId: Long) = repository.getPhotosWithLocationOnce(activityId)
}