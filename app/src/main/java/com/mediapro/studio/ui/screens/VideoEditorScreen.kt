package com.mediapro.studio.ui.screens

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.mediapro.studio.domain.model.VideoTrimRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(UnstableApi::class)
@Composable
fun VideoEditorScreen(videoUri: Uri, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var duration by remember { mutableFloatStateOf(0f) }
    var start by remember { mutableFloatStateOf(0f) }
    var end by remember { mutableFloatStateOf(0f) }
    var status by remember { mutableStateOf("جارٍ تجهيز معاينة الفيديو") }
    var exporting by remember { mutableStateOf(false) }
    val minimumClipMs = duration.coerceAtMost(500f).coerceAtLeast(1f)

    val player = remember(videoUri) { ExoPlayer.Builder(context).build() }
    DisposableEffect(player, videoUri) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY && player.duration > 0) {
                    duration = player.duration.toFloat()
                    end = player.duration.toFloat()
                    status = "الفيديو جاهز للقص"
                }
            }
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                status = "تعذّرت معاينة الفيديو: ${error.errorCodeName}"
            }
        }
        player.addListener(listener)
        player.setMediaItem(MediaItem.fromUri(videoUri))
        player.prepare()
        player.playWhenReady = false
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(onClick = onBack) { Text("رجوع") }
            Text("محرر الفيديو", style = MaterialTheme.typography.titleLarge)
        }
        AndroidView(
            factory = { viewContext ->
                PlayerView(viewContext).apply {
                    player = player
                    useController = true
                }
            },
            update = { it.player = player },
            modifier = Modifier.fillMaxWidth().height(240.dp)
        )
        Text("بداية المقطع: ${formatTime(start.toLong())}")
        Slider(
            value = start,
            onValueChange = { start = it.coerceIn(0f, (end - minimumClipMs).coerceAtLeast(0f)) },
            valueRange = 0f..(duration.coerceAtLeast(1f))
        )
        Text("نهاية المقطع: ${formatTime(end.toLong())}")
        Slider(
            value = end.coerceIn(0f, duration.coerceAtLeast(1f)),
            onValueChange = { end = it.coerceIn((start + minimumClipMs).coerceAtMost(duration), duration.coerceAtLeast(1f)) },
            valueRange = 0f..(duration.coerceAtLeast(1f))
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = { player.seekTo(start.toLong()); player.play() },
                enabled = duration > 0f,
                modifier = Modifier.weight(1f)
            ) { Text("معاينة من البداية") }
            Button(
                onClick = {
                    val range = VideoTrimRange(start.toLong(), end.toLong(), duration.toLong())
                    if (!range.isValid) {
                        status = "انتظر تحميل الفيديو وحدد نطاقاً صالحاً داخل مدة الملف."
                    } else {
                        exporting = true
                        status = "جارٍ تصدير المقطع..."
                        exportTrimmedVideo(context, videoUri, range.startMs, range.endMs, scope) { result ->
                            exporting = false
                            status = result
                        }
                    }
                },
                enabled = duration > 0f && !exporting,
                modifier = Modifier.weight(1f)
            ) {
                if (exporting) CircularProgressIndicator()
                else Text("تصدير القصاصة")
            }
        }
        Text(status, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
        Text(
            "هذه المرحلة تدعم معاينة الفيديو وقص مقطع واحد. الدمج والمسارات المتعددة والانتقالات والصوت ستضاف لاحقاً.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@OptIn(UnstableApi::class)
private fun exportTrimmedVideo(
    context: Context,
    source: Uri,
    startMs: Long,
    endMs: Long,
    scope: kotlinx.coroutines.CoroutineScope,
    onFinished: (String) -> Unit
) {
    val temp = File(context.cacheDir, "mediapro_clip_${System.currentTimeMillis()}.mp4")
    val clipped = MediaItem.Builder()
        .setUri(source)
        .setClippingConfiguration(
            MediaItem.ClippingConfiguration.Builder()
                .setStartPositionMs(startMs)
                .setEndPositionMs(endMs)
                .build()
        )
        .build()
    val transformer = Transformer.Builder(context).addListener(object : Transformer.Listener {
        override fun onCompleted(composition: Composition, exportResult: ExportResult) {
            scope.launch {
                val result = withContext(Dispatchers.IO) { publishVideo(context, temp) }
                onFinished(result)
            }
        }
        override fun onError(
            composition: Composition,
            exportResult: ExportResult,
            exportException: ExportException
        ) {
            temp.delete()
            onFinished("فشل تصدير الفيديو: ${exportException.localizedMessage ?: "خطأ غير معروف"}")
        }
    }).build()
    try {
        transformer.start(EditedMediaItem.Builder(clipped).build(), temp.absolutePath)
    } catch (error: Exception) {
        temp.delete()
        onFinished("تعذّر بدء التصدير: ${error.localizedMessage ?: "خطأ غير معروف"}")
    }
}

private fun publishVideo(context: Context, source: File): String {
    if (!source.exists() || source.length() == 0L) return "ملف التصدير غير موجود."
    val resolver = context.contentResolver
    val metadata = ContentValues().apply {
        put(MediaStore.Video.Media.DISPLAY_NAME, "MediaPro_${System.currentTimeMillis()}.mp4")
        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/MediaProStudio")
        put(MediaStore.Video.Media.IS_PENDING, 1)
    }
    val destination = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, metadata)
        ?: run { source.delete(); return "تعذّر إنشاء ملف الفيديو." }
    return try {
        resolver.openOutputStream(destination)?.use { output ->
            source.inputStream().use { input -> input.copyTo(output) }
        } ?: error("Output stream unavailable")
        val published = ContentValues().apply { put(MediaStore.Video.Media.IS_PENDING, 0) }
        resolver.update(destination, published, null, null)
        source.delete()
        "تم حفظ الفيديو في Movies/MediaProStudio."
    } catch (error: Exception) {
        resolver.delete(destination, null, null)
        source.delete()
        "فشل حفظ الفيديو: ${error.localizedMessage ?: "خطأ غير معروف"}"
    }
}

private fun formatTime(milliseconds: Long): String {
    val seconds = (milliseconds / 1000L).coerceAtLeast(0L)
    return "%02d:%02d".format(seconds / 60L, seconds % 60L)
}
