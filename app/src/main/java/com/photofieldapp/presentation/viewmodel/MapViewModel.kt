package com.photofieldapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.photofieldapp.domain.usecase.GetPhotosWithLocationUseCase
import com.photofieldapp.presentation.ui.map.LatLng
import com.photofieldapp.presentation.ui.map.CameraPosition
import com.photofieldapp.presentation.ui.map.PhotoMarker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getPhotosWithLocationUseCase: GetPhotosWithLocationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: androidx.lifecycle.flow.StateFlow<MapUiState> = _uiState.asStateFlow()

    data class MapUiState(
        val photoMarkers: List<PhotoMarker> = emptyList(),
        val selectedMarker: PhotoMarker? = null,
        val cameraPosition: CameraPosition? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    fun loadPhotosForProject(projectId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // This would need a project-level photo query - for now use activity-based
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun loadPhotosForActivity(activityId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            val photos = getPhotosWithLocationUseCase.invokeOnce(activityId)

            val markers = photos.map { photo ->
                PhotoMarker(
                    id = photo.id,
                    position = LatLng(photo.latitude, photo.longitude),
                    title = "Foto ${photo.sortOrder + 1}",
                    snippet = "${photo.fileName}\n${android.text.format.DateFormat.format("dd MMM yyyy HH:mm", photo.timestamp)}",
                    photo = photo
                )
            }

            // Calculate bounds for camera
            var cameraPos: CameraPosition? = null
            if (markers.isNotEmpty()) {
                val avgLat = markers.map { it.position.latitude }.average()
                val avgLng = markers.map { it.position.longitude }.average()
                cameraPos = CameraPosition(LatLng(avgLat, avgLng))
            }

            _uiState.update { it.copy(photoMarkers = markers, cameraPosition = cameraPos, isLoading = false) }
        }
    }

    fun selectMarker(marker: PhotoMarker?) {
        _uiState.update { it.copy(selectedMarker = marker) }
    }

    fun updateCameraPosition(position: CameraPosition) {
        _uiState.update { it.copy(cameraPosition = position) }
    }

    fun centerOnMarkers() {
        val markers = _uiState.value.photoMarkers
        if (markers.isNotEmpty()) {
            val avgLat = markers.map { it.position.latitude }.average()
            val avgLng = markers.map { it.position.longitude }.average()
            _uiState.update { it.copy(cameraPosition = CameraPosition(LatLng(avgLat, avgLng), 15.0)) }
        }
    }

    fun setError(error: String?) {
        _uiState.update { it.copy(error = error) }
    }
}