package com.mediapro.studio.ui.screens

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Matrix
import android.graphics.ColorMatrix as AndroidColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ImageEditorScreen(imageUri: Uri, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var brightness by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(1f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var overlayText by remember { mutableStateOf("") }
    var exporting by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    val matrix = remember(brightness, contrast, saturation) {
        val b = brightness * 2.55f
        val c = contrast
        val s = saturation
        floatArrayOf(
            c * (0.213f * s + 0.787f), c * (0.715f * s - 0.715f), c * (0.072f * s - 0.072f), 0f, b,
            c * (0.213f * s - 0.213f), c * (0.715f * s + 0.285f), c * (0.072f * s - 0.072f), 0f, b,
            c * (0.213f * s - 0.213f), c * (0.715f * s - 0.715f), c * (0.072f * s + 0.928f), 0f, b,
            0f, 0f, 0f, 1f, 0f
        )
    }
    val previewFilter = remember(matrix) { ColorFilter.colorMatrix(ColorMatrix(matrix)) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = onBack) { Text("رجوع") }
            Text("محرر الصور", style = MaterialTheme.typography.titleLarge)
        }
        Box(
            Modifier.fillMaxWidth().height(320.dp).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = "معاينة الصورة",
                modifier = Modifier.fillMaxSize().graphicsLayer(rotationZ = rotation),
                contentScale = ContentScale.Fit,
                colorFilter = previewFilter
            )
            if (overlayText.isNotBlank()) {
                Text(
                    overlayText,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { rotation = (rotation - 90f + 360f) % 360f }, modifier = Modifier.weight(1f)) {
                Text("تدوير لليسار")
            }
            OutlinedButton(onClick = { rotation = (rotation + 90f) % 360f }, modifier = Modifier.weight(1f)) {
                Text("تدوير لليمين")
            }
        }
        TextField(
            value = overlayText,
            onValueChange = { overlayText = it.take(120) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("نص على الصورة (اختياري)") },
            singleLine = true
        )
        AdjustmentControl("السطوع", brightness, -100f, 100f, true) { brightness = it }
        AdjustmentControl("التباين", contrast, 0.5f, 2f) { contrast = it }
        AdjustmentControl("التشبّع", saturation, 0f, 2f) { saturation = it }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = { brightness = 0f; contrast = 1f; saturation = 1f; rotation = 0f; overlayText = ""; message = null },
                modifier = Modifier.weight(1f)
            ) { Text("إعادة الضبط") }
            Button(
                onClick = {
                    exporting = true
                    message = null
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            exportAdjustedImage(context, imageUri, matrix, rotation, overlayText)
                        }
                        exporting = false
                        message = result
                    }
                },
                enabled = !exporting,
                modifier = Modifier.weight(1f)
            ) {
                if (exporting) CircularProgressIndicator()
                else Text("تصدير الصورة")
            }
        }
        message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        Text(
            "تُطبّق التعديلات على نسخة جديدة عند التصدير، ولا يتغير الملف الأصلي.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AdjustmentControl(
    label: String,
    value: Float,
    minimum: Float,
    maximum: Float,
    integerValue: Boolean = false,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(if (integerValue) value.toInt().toString() else "%.2f".format(value))
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = minimum..maximum)
    }
}

private fun exportAdjustedImage(
    context: android.content.Context,
    source: Uri,
    matrixValues: FloatArray,
    rotation: Float,
    overlayText: String
): String {
    val original = context.contentResolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it) }
        ?: return "تعذّر فتح الصورة."
    val output = try {
        val transform = Matrix().apply { postRotate(rotation) }
        val rotated = Bitmap.createBitmap(original, 0, 0, original.width, original.height, transform, true)
        val bitmap = Bitmap.createBitmap(rotated.width, rotated.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(AndroidColorMatrix(matrixValues))
        }
        canvas.drawBitmap(rotated, 0f, 0f, imagePaint)
        rotated.recycle()
        if (overlayText.isNotBlank()) {
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                textSize = (bitmap.width.coerceAtMost(bitmap.height) * 0.055f).coerceAtLeast(24f)
                setShadowLayer(4f, 2f, 2f, android.graphics.Color.BLACK)
                textAlign = Paint.Align.CENTER
            }
            val x = bitmap.width / 2f
            val y = bitmap.height - bitmap.height * 0.06f
            canvas.drawText(overlayText, x, y, textPaint)
        }
        bitmap
    } catch (error: Exception) {
        original.recycle()
        return "تعذّر تجهيز الصورة: ${error.localizedMessage ?: "خطأ غير معروف"}"
    }
    original.recycle()

    val filename = "MediaPro_${System.currentTimeMillis()}.jpg"
    val metadata = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MediaProStudio")
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }
    val resolver = context.contentResolver
    val destination = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, metadata)
        ?: run { output.recycle(); return "تعذّر إنشاء ملف التصدير." }
    return try {
        resolver.openOutputStream(destination)?.use { stream ->
            check(output.compress(Bitmap.CompressFormat.JPEG, 95, stream)) { "Image compression failed" }
        } ?: error("Output stream unavailable")
        val published = ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }
        resolver.update(destination, published, null, null)
        output.recycle()
        "تم حفظ الصورة في Pictures/MediaProStudio."
    } catch (error: Exception) {
        resolver.delete(destination, null, null)
        output.recycle()
        "فشل التصدير: ${error.localizedMessage ?: "خطأ غير معروف"}"
    }
}
