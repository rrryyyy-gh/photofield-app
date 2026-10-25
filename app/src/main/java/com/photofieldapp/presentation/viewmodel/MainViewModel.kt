package com.photofieldapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.photofieldapp.data.repository.ActivityRepository
import com.photofieldapp.data.repository.PhotoRepository
import com.photofieldapp.data.repository.ProjectRepository
import com.photofieldapp.domain.usecase.*
import com.photofieldapp.util.FileStorageUtil
import com.photofieldapp.util.PdfGeneratorUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getProjectsUseCase: GetProjectsUseCase,
    private val createProjectUseCase: CreateProjectUseCase,
    private val updateProjectUseCase: UpdateProjectUseCase,
    private val deleteProjectUseCase: DeleteProjectUseCase,
    private val projectRepository: ProjectRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: androidx.lifecycle.flow.StateFlow<MainUiState> = _uiState.asStateFlow()

    data class MainUiState(
        val projects: List<com.photofieldapp.domain.model.Project> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val showCreateDialog: Boolean = false
    )

    init {
        loadProjects()
    }

    private fun loadProjects() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getProjectsUseCase().onEach { projects ->
                _uiState.update { it.copy(projects = projects, isLoading = false) }
            }.launchIn(viewModelScope)
        }
    }

    fun createProject(name: String, description: String = "", locationName: String = "", latitude: Double = 0.0, longitude: Double = 0.0) {
        viewModelScope.launch(Dispatchers.IO) {
            val project = com.photofieldapp.domain.model.Project.create(
                name = name,
                description = description,
                locationName = locationName,
                locationLatitude = latitude,
                locationLongitude = longitude
            )
            createProjectUseCase(project)
            _uiState.update { it.copy(showCreateDialog = false) }
        }
    }

    fun updateProject(project: com.photofieldapp.domain.model.Project) {
        viewModelScope.launch(Dispatchers.IO) {
            updateProjectUseCase(project)
        }
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteProjectUseCase(projectId)
        }
    }

    fun setShowCreateDialog(show: Boolean) {
        _uiState.update { it.copy(showCreateDialog = show, error = null) }
    }

    fun setError(error: String?) {
        _uiState.update { it.copy(error = error) }
    }
}