package com.mediapro.studio.ui.screens

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.ColorMatrix as AndroidColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
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
        AsyncImage(
            model = imageUri,
            contentDescription = "معاينة الصورة",
            modifier = Modifier.fillMaxWidth().height(320.dp).background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Fit,
            colorFilter = previewFilter
        )
        AdjustmentControl("السطوع", brightness, -100f, 100f, true) { brightness = it }
        AdjustmentControl("التباين", contrast, 0.5f, 2f) { contrast = it }
        AdjustmentControl("التشبّع", saturation, 0f, 2f) { saturation = it }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = { brightness = 0f; contrast = 1f; saturation = 1f; message = null },
                modifier = Modifier.weight(1f)
            ) { Text("إعادة الضبط") }
            Button(
                onClick = {
                    exporting = true
                    message = null
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            exportAdjustedImage(context, imageUri, matrix)
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
    matrixValues: FloatArray
): String {
    val original = context.contentResolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it) }
        ?: return "تعذّر فتح الصورة."
    val output = try {
        Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888).also { bitmap ->
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                colorFilter = ColorMatrixColorFilter(AndroidColorMatrix(matrixValues))
            }
            Canvas(bitmap).drawBitmap(original, 0f, 0f, paint)
        }
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
