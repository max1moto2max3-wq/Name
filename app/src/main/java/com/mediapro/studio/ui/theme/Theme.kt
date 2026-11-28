package com.mediapro.studio.ui.theme
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
private val StudioDarkColors = darkColorScheme(
    primary = Color(0xFF9ADCF8), secondary = Color(0xFFC3C7FF),
    tertiary = Color(0xFFB5E4D5), background = Color(0xFF0B0E14),
    surface = Color(0xFF151A23)
)
@Composable
fun MediaProTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) dynamicDarkColorScheme(context) else StudioDarkColors
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(colorScheme = colors, content = content)
    }
}
