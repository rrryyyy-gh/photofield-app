package com.photofieldapp.domain.usecase

import com.photofieldapp.data.repository.ProjectRepository
import com.photofieldapp.domain.model.Project
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProjectsUseCase @Inject constructor(
    private val repository: ProjectRepository
) {
    operator fun invoke(): Flow<List<Project>> = repository.getAllProjects()
}

class GetProjectUseCase @Inject constructor(
    private val repository: ProjectRepository
) {
    operator fun invoke(projectId: Long): Project? = repository.getProjectById(projectId)
}

class CreateProjectUseCase @Inject constructor(
    private val repository: ProjectRepository
) {
    operator fun invoke(project: Project): Long = repository.createProject(project)
}

class UpdateProjectUseCase @Inject constructor(
    private val repository: ProjectRepository
) {
    operator fun invoke(project: Project) = repository.updateProject(project)
}

class DeleteProjectUseCase @Inject constructor(
    private val repository: ProjectRepository
) {
    operator fun invoke(projectId: Long) = repository.deleteProject(projectId)
}