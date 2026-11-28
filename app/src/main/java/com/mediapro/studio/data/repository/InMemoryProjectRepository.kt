package com.mediapro.studio.data.repository
import com.mediapro.studio.domain.model.MediaProject
import com.mediapro.studio.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class InMemoryProjectRepository @Inject constructor() : ProjectRepository {
    private val projects = MutableStateFlow<List<MediaProject>>(emptyList())
    override fun observeProjects(): Flow<List<MediaProject>> = projects.asStateFlow()
    override suspend fun addProject(project: MediaProject) {
        projects.update { current -> listOf(project) + current.filterNot { it.uri == project.uri } }
    }
}
