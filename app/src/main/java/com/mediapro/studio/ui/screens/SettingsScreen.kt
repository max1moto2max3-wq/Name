package com.mediapro.studio.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mediapro.studio.BuildConfig

@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("الإعدادات", style = MaterialTheme.typography.headlineMedium)
        Text("MediaPro Studio", style = MaterialTheme.typography.titleLarge)
        Text(
            "الإصدار ${BuildConfig.VERSION_NAME} (رقم البناء ${BuildConfig.VERSION_CODE})",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "ستضاف إعدادات التصدير والخصوصية واللغة في المراحل التالية.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
