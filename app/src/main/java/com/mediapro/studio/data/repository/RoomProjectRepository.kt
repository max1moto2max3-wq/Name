package com.mediapro.studio.data.repository

import com.mediapro.studio.data.local.ProjectDao
import com.mediapro.studio.data.local.ProjectEntity
import com.mediapro.studio.domain.model.MediaProject
import com.mediapro.studio.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomProjectRepository @Inject constructor(private val dao: ProjectDao) : ProjectRepository {
    override fun observeProjects(): Flow<List<MediaProject>> =
        dao.observeAll().map { rows -> rows.map(ProjectEntity::toDomain) }

    override suspend fun addProject(project: MediaProject) {
        dao.insert(ProjectEntity.fromDomain(project))
    }

    override suspend fun deleteProject(id: String) {
        dao.deleteById(id)
    }
}
