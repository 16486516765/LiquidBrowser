package com.limao.liquidbrowser.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.limao.liquidbrowser.features.settings.ThemeMode

private val LightColors = lightColorScheme(
    primary = Color(0xFF6D4AFF),
    onPrimary = Color.White,
    background = Color(0xFFF4F7FC),
    onBackground = Color(0xFF1A1A22),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1A22),
    surfaceVariant = Color(0xFFE8ECF4),
    onSurfaceVariant = Color(0xFF55565E),
    outline = Color(0xFFD5D9E2),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB79CFF),
    onPrimary = Color(0xFF1A1033),
    background = Color(0xFF101018),
    onBackground = Color(0xFFF2F2F7),
    surface = Color(0xFF1C1C26),
    onSurface = Color(0xFFF2F2F7),
    surfaceVariant = Color(0xFF2A2A38),
    onSurfaceVariant = Color(0xFFB0B0BE),
    outline = Color(0xFF3A3A48),
)

/** 阶段 1 主题：浅色 / 深色 / 跟随系统。阶段 4+ 把用户偏好持久化到 DataStore。 */
@Composable
fun LiquidBrowserTheme(
    themeMode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
