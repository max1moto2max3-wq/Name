package com.mediapro.studio
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import com.mediapro.studio.ui.navigation.MediaProApp
import com.mediapro.studio.ui.theme.MediaProTheme
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MediaProTheme { MediaProApp() } }
    }
}
