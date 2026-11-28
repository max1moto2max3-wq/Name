package com.mediapro.studio.domain.model

/**
 * A validated, half-open trim selection expressed in milliseconds.
 * The end position must be after the start and within the source duration.
 */
data class VideoTrimRange(
    val startMs: Long,
    val endMs: Long,
    val durationMs: Long
) {
    val isValid: Boolean
        get() = durationMs > 0L &&
            startMs >= 0L &&
            endMs > startMs &&
            endMs <= durationMs
}
