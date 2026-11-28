package com.mediapro.studio.data.local
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mediapro.studio.domain.model.MediaKind
import com.mediapro.studio.domain.model.MediaProject
@Entity(tableName = "media_projects", indices = [Index(value = ["uri"], unique = true)])
data class ProjectEntity(
    @PrimaryKey val id: String,
    val uri: String,
    val displayName: String,
    val kind: String,
    val createdAt: Long
) {
    fun toDomain() = MediaProject(id, uri, displayName, MediaKind.valueOf(kind), createdAt)
    companion object {
        fun fromDomain(project: MediaProject) = ProjectEntity(
            project.id, project.uri, project.displayName, project.kind.name, project.createdAt
        )
    }
}
