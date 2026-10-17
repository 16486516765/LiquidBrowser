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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.limao.liquidbrowser.ui.components.GlassSearchBar
import com.limao.liquidbrowser.ui.theme.GlassTokens

private val QuickLinks = listOf(
    "Bilibili" to "https://www.bilibili.com",
    "GitHub" to "https://github.com",
    "知乎" to "https://www.zhihu.com",
    "维基百科" to "https://zh.wikipedia.org",
)

/** 首页：胶囊玻璃搜索框 + 快捷链接。输入后进入浏览器界面，由 WebView 真正加载。 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onOpenQuery: (String) -> Unit,
    backdrop: Backdrop,
    glassIntensity: Float = 1f,
) {
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
        GlassSearchBar(
            value = query,
            onValueChange = { query = it },
            onSubmit = ::submit,
            backdrop = backdrop,
            glassIntensity = glassIntensity,
        )
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
                "底部导航栏与搜索框已接入 Backdrop 真实折射（iOS 风格）。" +
                    "玻璃压在 WebView 上时无法折射网页内容，属已知限制，见 README。",
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
