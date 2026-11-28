package com.mediapro.studio.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val StudioDarkColors = darkColorScheme(
    primary = Color(0xFF8BE0D2),
    onPrimary = Color(0xFF062A27),
    primaryContainer = Color(0xFF164B45),
    onPrimaryContainer = Color(0xFFC0FFF3),
    secondary = Color(0xFFB9C4FF),
    onSecondary = Color(0xFF202B58),
    secondaryContainer = Color(0xFF354477),
    onSecondaryContainer = Color(0xFFE0E5FF),
    tertiary = Color(0xFFFFB68A),
    background = Color(0xFF0A1015),
    onBackground = Color(0xFFE6F0F2),
    surface = Color(0xFF10191F),
    onSurface = Color(0xFFE6F0F2),
    surfaceVariant = Color(0xFF202D34),
    onSurfaceVariant = Color(0xFFB5C6CB),
    outline = Color(0xFF52666D)
)

private val StudioLightColors = lightColorScheme(
    primary = Color(0xFF006B61),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF9DF2E2),
    onPrimaryContainer = Color(0xFF00201C),
    secondary = Color(0xFF4C5C91),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDE1FF),
    onSecondaryContainer = Color(0xFF071747),
    tertiary = Color(0xFF8A4D22),
    background = Color(0xFFF5F8F7),
    onBackground = Color(0xFF171D1C),
    surface = Color(0xFFFBFDFC),
    onSurface = Color(0xFF171D1C),
    surfaceVariant = Color(0xFFE0E9E6),
    onSurfaceVariant = Color(0xFF414C49),
    outline = Color(0xFF707D79)
)

@Composable
fun MediaProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) StudioDarkColors else StudioLightColors
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colors,
            content = content
        )
    }
}
