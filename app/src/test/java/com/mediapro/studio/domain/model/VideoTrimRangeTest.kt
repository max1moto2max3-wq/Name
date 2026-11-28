package com.mediapro.studio.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoTrimRangeTest {
    @Test
    fun acceptsRangeInsideSourceDuration() {
        assertTrue(VideoTrimRange(500L, 2_500L, 5_000L).isValid)
    }

    @Test
    fun rejectsEmptyOrReversedRange() {
        assertFalse(VideoTrimRange(1_000L, 1_000L, 5_000L).isValid)
        assertFalse(VideoTrimRange(2_000L, 1_000L, 5_000L).isValid)
    }

    @Test
    fun rejectsNegativeStartAndEndBeyondDuration() {
        assertFalse(VideoTrimRange(-1L, 1_000L, 5_000L).isValid)
        assertFalse(VideoTrimRange(1_000L, 5_001L, 5_000L).isValid)
    }

    @Test
    fun rejectsUnknownOrZeroDuration() {
        assertFalse(VideoTrimRange(0L, 1_000L, 0L).isValid)
    }
}
