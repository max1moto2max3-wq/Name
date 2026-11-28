package com.mediapro.studio.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoSettings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private data class StudioTool(
    val title: String,
    val description: String,
    val category: String,
    val available: Boolean
)

private val plannedTools = listOf(
    StudioTool("قص وتدوير الصور", "تعديل الأبعاد ونسب العرض والارتفاع", "الصور", true),
    StudioTool("تحسين الألوان", "السطوع والتباين والتشبع", "الصور", true),
    StudioTool("إضافة نص للصورة", "كتابة نص فوق الصورة", "الصور", true),
    StudioTool("قص الفيديو", "تحديد نقطة البداية والنهاية وتصدير MP4", "الفيديو", true),
    StudioTool("دمج المقاطع والمسارات", "ترتيب عدة مقاطع على خط زمني", "الفيديو", false),
    StudioTool("الانتقالات والمؤثرات", "انتقالات بين المشاهد ومؤثرات بصرية", "الفيديو", false),
    StudioTool("تحرير الصوت", "قص الصوت والتحكم بمستوى الصوت", "الصوت", false),
    StudioTool("الترجمة والعناوين", "مسارات ترجمة وعناوين قابلة للتنسيق", "الصوت", false),
    StudioTool("الطبقات والملصقات", "صور ونصوص وعناصر متعددة الطبقات", "التصميم", false),
    StudioTool("الفلاتر والإعدادات المسبقة", "مظهر موحد وحفظ إعداداتك", "التصميم", false),
    StudioTool("تغيير المقاس والضغط", "تصدير بأبعاد وجودة وحجم مناسب", "التصدير", false),
    StudioTool("صانع الصور المصغرة", "قوالب مصغرات لمنصات الفيديو", "التصميم", false)
)

@Composable
fun ToolsScreen(
    onPickImage: () -> Unit,
    onPickVideo: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                Text("مركز الأدوات", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "أدوات تحرير الصور والفيديو والصوت والتصميم. الأدوات المتاحة موضحة بوضوح.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(plannedTools.groupBy { it.category }.toList(), key = { it.first }) { (category, tools) ->
            Text(category, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))
            tools.forEach { tool ->
                val icon = when (category) {
                    "الصور" -> if (tool.title.contains("قص")) Icons.Filled.Crop else Icons.Filled.Photo
                    "الفيديو" -> Icons.Filled.Movie
                    "الصوت" -> Icons.Filled.VideoSettings
                    else -> Icons.Filled.AutoFixHigh
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (tool.available && category == "الصور") onPickImage()
                        if (tool.available && category == "الفيديو") onPickVideo()
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(tool.title, style = MaterialTheme.typography.titleMedium)
                            Text(tool.description, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            if (tool.available) "متاحة" else "قريباً",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (tool.available) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
