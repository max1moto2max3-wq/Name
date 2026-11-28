package com.mediapro.studio.ui.viewmodel
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediapro.studio.domain.model.MediaKind
import com.mediapro.studio.domain.model.MediaProject
import com.mediapro.studio.domain.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
                MediaProject(UUID.randomUUID().toString(), uri.toString(),
                    displayName.ifBlank { "وسائط جديدة" }, kind, System.currentTimeMillis())
            )
        }
    }
}
