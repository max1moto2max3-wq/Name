package com.mediapro.studio.ui.screens
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mediapro.studio.domain.model.MediaProject
@Composable
fun HomeScreen(projects: List<MediaProject>, onMediaPicked: (Uri, String?, String) -> Unit) {
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val mime = context.contentResolver.getType(uri)
            val name = uri.lastPathSegment?.substringAfterLast('/') ?: "وسائط جديدة"
            onMediaPicked(uri, mime, name)
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("MediaPro Studio", style = MaterialTheme.typography.headlineMedium)
                Text("مساحة واحدة لتحرير الصور والفيديو", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            ElevatedCard(shape = RoundedCornerShape(24.dp), colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("ابدأ مشروعك", style = MaterialTheme.typography.titleLarge)
                    Text("استورد صورة أو مقطع فيديو من جهازك. تتم المعالجة محلياً في هذه المرحلة.",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            modifier = Modifier.weight(1f)) {
                            androidx.compose.material3.Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null)
                            Text("صورة")
                        }
                        OutlinedButton(onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) },
                            modifier = Modifier.weight(1f)) {
                            androidx.compose.material3.Icon(Icons.Filled.Movie, contentDescription = null)
                            Text("فيديو")
                        }
                    }
                }
            }
        }
        item { Text("المشاريع الأخيرة", style = MaterialTheme.typography.titleLarge) }
        if (projects.isEmpty()) {
            item { Card { Text("لا توجد مشاريع بعد. استورد صورة أو فيديو للبدء.", Modifier.padding(18.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        } else {
            items(projects.take(5), key = { it.id }) { project ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(project.displayName, style = MaterialTheme.typography.titleMedium)
                        Text(if (project.kind.name == "VIDEO") "فيديو" else "صورة",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}
