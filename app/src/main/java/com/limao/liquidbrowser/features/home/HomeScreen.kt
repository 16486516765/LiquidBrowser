package com.limao.liquidbrowser.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limao.liquidbrowser.ui.theme.GlassTokens

private val QuickLinks = listOf(
    "Bilibili" to "https://www.bilibili.com",
    "GitHub" to "https://github.com",
    "知乎" to "https://www.zhihu.com",
    "维基百科" to "https://zh.wikipedia.org",
)

/** 阶段 1 首页：搜索框 + 快捷链接。输入后进入浏览器界面，阶段 2 由 WebView 真正加载。 */
@Composable
fun HomeScreen(modifier: Modifier = Modifier, onOpenQuery: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    fun submit() {
        val q = query.trim()
        if (q.isNotEmpty()) onOpenQuery(q)
    }
    Column(
        modifier
            .fillMaxSize()
            .padding(GlassTokens.pagePadding),
    ) {
        Text(
            "早上好",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "输入网址或关键词开始浏览",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("输入网址或搜索关键词") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = { submit() }),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = ::submit, modifier = Modifier.fillMaxWidth()) {
            Text("搜索 / 前往")
        }
        Spacer(Modifier.height(24.dp))
        Text("快捷链接", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            QuickLinks.forEach { (name, url) ->
                Box(
                    Modifier
                        .clip(RoundedCornerShape(GlassTokens.chipCornerRadius))
                        .background(
                            MaterialTheme.colorScheme.surface.copy(
                                alpha = GlassTokens.surfaceAlphaLight,
                            ),
                        )
                        .border(
                            GlassTokens.borderWidth,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            RoundedCornerShape(GlassTokens.chipCornerRadius),
                        )
                        .clickable { onOpenQuery(url) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    Text(name, fontSize = 13.sp)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        // 阶段 1 占位卡：说明玻璃效果的当前状态，不假装已实现
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(GlassTokens.cardCornerRadius))
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                )
                .padding(16.dp),
        ) {
            Text(
                "液态玻璃：阶段 1 使用半透明材质占位。阶段 3 按已验证的 Backdrop API " +
                    "接入真实折射；若 WebView 背景无法被采样，将改用明确标注的视觉降级方案。",
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
