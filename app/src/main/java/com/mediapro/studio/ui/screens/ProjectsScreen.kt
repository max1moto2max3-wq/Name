package com.mediapro.studio.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mediapro.studio.domain.model.MediaProject

@Composable
fun ProjectsScreen(projects: List<MediaProject>, onProjectClick: (MediaProject) -> Unit) {
    if (projects.isEmpty()) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text("مشاريعك", style = MaterialTheme.typography.headlineMedium)
            Text("ستظهر ملفاتك المستوردة هنا.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Text("مشاريعك", style = MaterialTheme.typography.headlineMedium) }
            items(projects, key = { it.id }) { project ->
                Card(onClick = { onProjectClick(project) }) {
                    Column(Modifier.padding(16.dp)) {
                        Text(project.displayName, style = MaterialTheme.typography.titleMedium)
                        Text(if (project.kind.name == "VIDEO") "مشروع فيديو" else "مشروع صورة",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
