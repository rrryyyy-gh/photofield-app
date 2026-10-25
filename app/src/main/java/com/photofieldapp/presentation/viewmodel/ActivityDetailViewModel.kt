package com.photofieldapp.presentation.viewmodel

import android.app.Application
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.photofieldapp.data.repository.PhotoRepository
import com.photofieldapp.domain.usecase.*
import com.photofieldapp.util.CameraUtil
import com.photofieldapp.util.FileStorageUtil
import com.photofieldapp.util.LocationUtil
import com.photofieldapp.util.PdfGeneratorUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityDetailViewModel @Inject constructor(
    application: Application,
    private val getPhotosUseCase: GetPhotosUseCase,
    private val getPhotosWithLocationUseCase: GetPhotosWithLocationUseCase,
    private val createPhotoUseCase: CreatePhotoUseCase,
    private val updatePhotoUseCase: UpdatePhotoUseCase,
    private val deletePhotoUseCase: DeletePhotoUseCase,
    private val deletePhotosByActivityUseCase: DeletePhotosByActivityUseCase,
    private val updatePhotoSortOrderUseCase: UpdatePhotoSortOrderUseCase,
    private val getNextPhotoSortOrderUseCase: GetNextPhotoSortOrderUseCase,
    private val getDefaultPdfTemplateUseCase: GetDefaultPdfTemplateUseCase,
    private val photoRepository: PhotoRepository,
    private val cameraUtil: CameraUtil,
    private val locationUtil: LocationUtil,
    private val activityId: Long
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ActivityDetailUiState())
    val uiState: androidx.lifecycle.flow.StateFlow<ActivityDetailUiState> = _uiState.asStateFlow()

    data class ActivityDetailUiState(
        val photos: List<com.photofieldapp.domain.model.Photo> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val isCameraActive: Boolean = false,
        val isCapturing: Boolean = false,
        val isGeneratingPdf: Boolean = false,
        val pdfProgress: PdfGeneratorUtil.GenerationProgress? = null,
        val showDeleteConfirm: Long = -1, // photoId to delete, -1 = none
        val selectedPhotoForMap: com.photofieldapp.domain.model.Photo? = null
    )

    init {
        loadPhotos()
    }

    private fun loadPhotos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getPhotosUseCase(activityId).onEach { photos ->
                _uiState.update { it.copy(photos = photos, isLoading = false) }
            }.launchIn(viewModelScope)
        }
    }

    fun onCameraReady(lifecycleOwner: androidx.lifecycle.LifecycleOwner, previewView: androidx.camera.view.PreviewView, imageCapture: ImageCapture) {
        _uiState.update { it.copy(isCameraActive = true) }
        cameraUtil.bindCamera(
            lifecycleOwner = lifecycleOwner,
            previewView = previewView,
            imageCapture = imageCapture,
            enableAutoFocus = true,
            callback = object : CameraUtil.CameraSetupCallback {
                override fun onCameraStarted() {
                    _uiState.update { it.copy(isCameraActive = true, error = null) }
                }

                override fun onError(error: String) {
                    _uiState.update { it.copy(isCameraActive = false, error = error) }
                }
            }
        )
    }

    fun onCameraReleased() {
        _uiState.update { it.copy(isCameraActive = false) }
    }

    fun takePhoto(
        imageCapture: ImageCapture,
        onResult: (Boolean, String?) -> Unit
    ) {
        _uiState.update { it.copy(isCapturing = true) }

        // Get location first
        locationUtil.getCurrentLocation(timeoutMs = 8000) { location ->
            // Take picture
            val photoFile = FileStorageUtil.createPhotoFile(getApplication(), activityId)
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            cameraUtil.takePicture(
                imageCapture = imageCapture,
                outputFileOptions = outputOptions,
                onSuccess = { result ->
                    val relativePath = FileStorageUtil.getRelativePath(getApplication(), photoFile)
                    val sortOrder = getNextPhotoSortOrderUseCase(activityId)

                    // Get image dimensions
                    var width = 0
                    var height = 0
                    var orientation = 0
                    try {
                        val bitmapFactory = android.graphics.BitmapFactory
                        val options = bitmapFactory.Options().apply { inJustDecodeBounds = true }
                        bitmapFactory.decodeFile(photoFile.absolutePath, options)
                        width = options.outWidth
                        height = options.outHeight
                    } catch (e: Exception) {
                        // Ignore
                    }

                    val photo = com.photofieldapp.domain.model.Photo.create(
                        activityId = activityId,
                        filePath = relativePath,
                        fileName = photoFile.name,
                        latitude = location?.latitude ?: 0.0,
                        longitude = location?.longitude ?: 0.0,
                        altitude = location?.altitude ?: 0.0,
                        accuracy = location?.accuracy ?: 0f,
                        bearing = location?.bearing ?: 0f,
                        timestamp = System.currentTimeMillis(),
                        width = width,
                        height = height,
                        sortOrder = sortOrder
                    )

                    viewModelScope.launch(Dispatchers.IO) {
                        createPhotoUseCase(photo)
                        _uiState.update { it.copy(isCapturing = false) }
                        onResult(true, null)
                    }
                },
                onError = { exception ->
                    _uiState.update { it.copy(isCapturing = false, error = "Gagal mengambil foto: ${exception.message}") }
                    onResult(false, exception.message)
                }
            )
        }
    }

    fun deletePhoto(photoId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            // Delete file
            getPhotosUseCase(activityId).first().find { it.id == photoId }?.let { photo ->
                FileStorageUtil.deleteFile(getApplication(), photo.filePath)
            }
            deletePhotoUseCase(photoId)
        }
    }

    fun setShowDeleteConfirm(photoId: Long) {
        _uiState.update { it.copy(showDeleteConfirm = photoId) }
    }

    fun confirmDeletePhoto(photoId: Long) {
        deletePhoto(photoId)
        _uiState.update { it.copy(showDeleteConfirm = -1) }
    }

    fun cancelDeletePhoto() {
        _uiState.update { it.copy(showDeleteConfirm = -1) }
    }

    fun reorderPhotos(fromIndex: Int, toIndex: Int) {
        val photos = _uiState.value.photos.toMutableList()
        if (fromIndex < 0 || fromIndex >= photos.size || toIndex < 0 || toIndex >= photos.size) return

        val photo = photos.removeAt(fromIndex)
        photos.add(toIndex, photo)

        // Update sort orders
        viewModelScope.launch(Dispatchers.IO) {
            photos.forEachIndexed { index, photo ->
                if (photo.sortOrder != index) {
                    val updated = photo.copyWithUpdated(sortOrder = index)
                    updatePhotoUseCase(updated)
                }
            }
        }
    }

    fun generatePdf(onResult: (Boolean, String?) -> Unit) {
        _uiState.update { it.copy(isGeneratingPdf = true, pdfProgress = null) }

        viewModelScope.launch(Dispatchers.IO) {
            val photos = getPhotosUseCase(activityId).first()
            if (photos.isEmpty()) {
                _uiState.update { it.copy(isGeneratingPdf = false, error = "Tidak ada foto untuk diekspor") }
                onResult(false, "Tidak ada foto")
                return@launch
            }

            val template = getDefaultPdfTemplateUseCase() ?: com.photofieldapp.domain.model.PdfTemplate.createDefault()
            
            // We need project and activity names - fetch them
            // For now, use placeholders
            val projectName = "Proyek"
            val activityName = "Activity"

            val result = PdfGeneratorUtil.generatePdf(
                context = getApplication(),
                projectName = projectName,
                activityName = activityName,
                photos = photos,
                template = template,
                progressCallback = { progress ->
                    _uiState.update { it.copy(pdfProgress = progress) }
                }
            )

            _uiState.update { it.copy(isGeneratingPdf = false) }

            if (result.success) {
                onResult(true, result.file?.absolutePath)
            } else {
                _uiState.update { it.copy(error = result.errorMessage ?: "Gagal membuat PDF") }
                onResult(false, result.errorMessage)
            }
        }
    }

    fun getPhotosWithLocation(): List<com.photofieldapp.domain.model.Photo> {
        return getPhotosWithLocationUseCase.invokeOnce(activityId)
    }

    fun setSelectedPhotoForMap(photo: com.photofieldapp.domain.model.Photo?) {
        _uiState.update { it.copy(selectedPhotoForMap = photo) }
    }

    fun setError(error: String?) {
        _uiState.update { it.copy(error = error) }
    }
}