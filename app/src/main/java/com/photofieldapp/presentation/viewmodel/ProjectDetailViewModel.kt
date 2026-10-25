package com.photofieldapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.photofieldapp.data.repository.ActivityRepository
import com.photofieldapp.domain.usecase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    private val getActivitiesUseCase: GetActivitiesUseCase,
    private val createActivityUseCase: CreateActivityUseCase,
    private val updateActivityUseCase: UpdateActivityUseCase,
    private val deleteActivityUseCase: DeleteActivityUseCase,
    private val getNextActivitySortOrderUseCase: GetNextActivitySortOrderUseCase,
    private val activityRepository: ActivityRepository,
    projectId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectDetailUiState())
    val uiState: androidx.lifecycle.flow.StateFlow<ProjectDetailUiState> = _uiState.asStateFlow()

    data class ProjectDetailUiState(
        val activities: List<com.photofieldapp.domain.model.Activity> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val showCreateDialog: Boolean = false,
        val editingActivity: com.photofieldapp.domain.model.Activity? = null
    )

    init {
        loadActivities()
    }

    private fun loadActivities() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getActivitiesUseCase(projectId).onEach { activities ->
                _uiState.update { it.copy(activities = activities, isLoading = false) }
            }.launchIn(viewModelScope)
        }
    }

    fun createActivity(name: String, description: String = "", locationName: String = "", latitude: Double = 0.0, longitude: Double = 0.0) {
        viewModelScope.launch(Dispatchers.IO) {
            val sortOrder = getNextActivitySortOrderUseCase(projectId)
            val activity = com.photofieldapp.domain.model.Activity.create(
                projectId = projectId,
                name = name,
                description = description,
                locationName = locationName,
                locationLatitude = latitude,
                locationLongitude = longitude,
                sortOrder = sortOrder
            )
            createActivityUseCase(activity)
            _uiState.update { it.copy(showCreateDialog = false) }
        }
    }

    fun updateActivity(activity: com.photofieldapp.domain.model.Activity) {
        viewModelScope.launch(Dispatchers.IO) {
            updateActivityUseCase(activity)
            _uiState.update { it.copy(editingActivity = null) }
        }
    }

    fun deleteActivity(activityId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteActivityUseCase(activityId)
        }
    }

    fun setShowCreateDialog(show: Boolean) {
        _uiState.update { it.copy(showCreateDialog = show, error = null) }
    }

    fun setEditingActivity(activity: com.photofieldapp.domain.model.Activity?) {
        _uiState.update { it.copy(editingActivity = activity, showCreateDialog = activity != null) }
    }

    fun setError(error: String?) {
        _uiState.update { it.copy(error = error) }
    }
}