package com.photofieldapp.data.repository

import com.photofieldapp.data.local.dao.ProjectDao
import com.photofieldapp.data.local.entity.Project
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepositoryImpl @Inject constructor(
    private val projectDao: ProjectDao
) : ProjectRepository {

    override suspend fun createProject(project: Project): Long {
        return projectDao.insert(project)
    }

    override suspend fun updateProject(project: Project) {
        projectDao.update(project)
    }

    override suspend fun deleteProject(projectId: Long) {
        projectDao.deleteById(projectId)
    }

    override fun getAllProjects(): Flow<List<Project>> {
        return projectDao.getAllProjects()
    }

    override suspend fun getProjectById(projectId: Long): Project? {
        return projectDao.getProjectById(projectId)
    }
}