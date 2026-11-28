package com.mediapro.studio.domain.model
data class MediaProject(
    val id: String,
    val uri: String,
    val displayName: String,
    val kind: MediaKind,
    val createdAt: Long
)
enum class MediaKind { IMAGE, VIDEO }
