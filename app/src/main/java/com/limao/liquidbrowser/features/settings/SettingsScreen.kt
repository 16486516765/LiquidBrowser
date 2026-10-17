package com.limao.liquidbrowser.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 主题模式：阶段 1 仅内存生效，阶段 4+ 持久化到 DataStore。 */
enum class ThemeMode(val label: String) {
    System("跟随系统"),
    Light("浅色"),
    Dark("深色"),
}

/** 阶段 1 设置：主题切换真实可用；玻璃强度为阶段 3 预留，当前仅保存数值。 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    glassIntensity: Float,
    onGlassIntensityChange: (Float) -> Unit,
) {
    Column(
        modifier
            .fillMaxSize()
            .padding(20.dp),
    ) {
        Text("主题模式", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        ThemeMode.entries.forEach { mode ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onThemeModeChange(mode) }
                    .padding(vertical = 8.dp),
            ) {
                RadioButton(
                    selected = themeMode == mode,
                    onClick = { onThemeModeChange(mode) },
                )
                Text(mode.label, modifier = Modifier.padding(start = 8.dp))
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("玻璃效果强度", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        Slider(
            value = glassIntensity,
            onValueChange = onGlassIntensityChange,
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            "当前：${(glassIntensity * 100).toInt()}%。映射为玻璃折射 / 模糊强度；" +
                "设为 0 关闭玻璃特效（纯半透明材质，低性能降级）。",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        Text("关于", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            "LiquidBrowser v1.0（阶段 1 骨架）\n" +
                "从零开发的独立项目，不是 Via 修改版。\n" +
                "阶段 3 将使用 Kyant0/AndroidLiquidGlass（Apache 2.0），" +
                "许可证说明将在关于页完整列出。",
            fontSize = 13.sp,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        CrashLogSection()
    }
}
