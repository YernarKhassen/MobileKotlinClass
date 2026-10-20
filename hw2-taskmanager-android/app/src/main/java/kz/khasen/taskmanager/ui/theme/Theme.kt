package kz.khasen.taskmanager.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Color(0xFF3B5BDB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBE4FF),
    onPrimaryContainer = Color(0xFF0B1A5C),
    secondary = Color(0xFF5F6B7A),
    secondaryContainer = Color(0xFFE2E8F0),
    onSecondaryContainer = Color(0xFF1C2430),
    tertiary = Color(0xFF0CA678),
    tertiaryContainer = Color(0xFFC3FAE8),
    onTertiaryContainer = Color(0xFF03301F),
    error = Color(0xFFE03131),
    errorContainer = Color(0xFFFFE3E3),
    onErrorContainer = Color(0xFF5C0A0A),
    background = Color(0xFFF7F8FB),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEEF1F6),
    onSurfaceVariant = Color(0xFF4A5565)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF91A7FF),
    onPrimary = Color(0xFF0B1A5C),
    primaryContainer = Color(0xFF2B3A8A),
    onPrimaryContainer = Color(0xFFDBE4FF),
    secondary = Color(0xFFB6C2D1),
    secondaryContainer = Color(0xFF3A4452),
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = Color(0xFF63E6BE),
    tertiaryContainer = Color(0xFF0B5C44),
    onTertiaryContainer = Color(0xFFC3FAE8),
    error = Color(0xFFFF8787),
    errorContainer = Color(0xFF7A1F1F),
    onErrorContainer = Color(0xFFFFE3E3),
    background = Color(0xFF0F131A),
    surface = Color(0xFF161B24),
    surfaceVariant = Color(0xFF232A36),
    onSurfaceVariant = Color(0xFFB6C2D1)
)

@Composable
fun TaskManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
