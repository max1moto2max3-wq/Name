package com.mediapro.studio.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediapro.studio.domain.model.MediaKind
import com.mediapro.studio.domain.model.MediaProject
import com.mediapro.studio.domain.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: ProjectRepository) : ViewModel() {
    val projects = repository.observeProjects().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun addMedia(uri: Uri, mimeType: String?, displayName: String) {
        viewModelScope.launch {
            val kind = if (mimeType?.startsWith("video/") == true) MediaKind.VIDEO else MediaKind.IMAGE
            repository.addProject(
                MediaProject(
                    UUID.randomUUID().toString(),
                    uri.toString(),
                    displayName.ifBlank { "وسائط جديدة" },
                    kind,
                    System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteProject(project: MediaProject) {
        viewModelScope.launch {
            repository.deleteProject(project.id)
            withContext(Dispatchers.IO) {
                runCatching {
                    val uri = Uri.parse(project.uri)
                    if (uri.scheme == "file") {
                        val importedDirectory = File(
                            // Imported media is deliberately stored under this app-private directory.
                            File(project.uri.removePrefix("file:")).parentFile?.parentFile,
                            "imported_media"
                        )
                        val candidate = File(uri.path ?: return@runCatching).canonicalFile
                        val allowedDirectory = importedDirectory.canonicalFile
                        if (candidate.parentFile == allowedDirectory) candidate.delete()
                    }
                }
            }
        }
    }
}
