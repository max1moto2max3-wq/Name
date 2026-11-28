package com.mediapro.studio.domain.repository

import com.mediapro.studio.domain.model.MediaProject
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    fun observeProjects(): Flow<List<MediaProject>>
    suspend fun addProject(project: MediaProject)
    suspend fun deleteProject(id: String)
}
