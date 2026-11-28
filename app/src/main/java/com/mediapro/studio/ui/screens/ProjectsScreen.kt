package com.mediapro.studio.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mediapro.studio.domain.model.MediaProject

@Composable
fun ProjectsScreen(
    projects: List<MediaProject>,
    onProjectClick: (MediaProject) -> Unit,
    onDeleteProject: (MediaProject) -> Unit
) {
    var projectToDelete by remember { mutableStateOf<MediaProject?>(null) }

    projectToDelete?.let { project ->
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = { Text("حذف المشروع؟") },
            text = { Text("سيُحذف المشروع والنسخة المستوردة من مساحة التطبيق. لا يمكن التراجع عن هذه الخطوة.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteProject(project)
                    projectToDelete = null
                }) { Text("حذف") }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) { Text("إلغاء") }
            }
        )
    }

    if (projects.isEmpty()) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text("مشاريعك", style = MaterialTheme.typography.headlineMedium)
            Text("ستظهر ملفاتك المستوردة هنا.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Text("مشاريعك", style = MaterialTheme.typography.headlineMedium) }
            items(projects, key = { it.id }) { project ->
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                project.displayName,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                if (project.kind.name == "VIDEO") "مشروع فيديو" else "مشروع صورة",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = { onProjectClick(project) }) {
                                Text("فتح المشروع")
                            }
                        }
                        OutlinedButton(onClick = { projectToDelete = project }) {
                            Text("حذف")
                        }
                    }
                }
            }
        }
    }
}
