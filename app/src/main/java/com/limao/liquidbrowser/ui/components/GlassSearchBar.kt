// 长条胶囊状玻璃搜索框：Backdrop 液态玻璃（vibrancy + blur + lens），iOS 风格透明度/折射。
// 首页与浏览器地址栏共用。玻璃强度为 0 时退回纯半透明材质。
package com.limao.liquidbrowser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.limao.liquidbrowser.ui.theme.GlassTokens

@Composable
fun GlassSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    backdrop: Backdrop,
    glassIntensity: Float = 1f,
    modifier: Modifier = Modifier,
    placeholder: String = "输入网址或搜索关键词",
) {
    val containerColor = MaterialTheme.colorScheme.surface.copy(alpha = GlassTokens.glassSurfaceAlpha)
    val glassEnabled = glassIntensity > 0.01f
    Box(
        modifier
            .then(
                if (glassEnabled) {
                    Modifier.drawBackdrop(
                        backdrop = backdrop,
                        shape = { CircleShape },
                        effects = {
                            vibrancy()
                            blur(GlassTokens.glassBlurRadius.toPx() * glassIntensity)
                            lens(
                                GlassTokens.glassRefractionHeight.toPx() * glassIntensity,
                                GlassTokens.glassRefractionAmount.toPx() * glassIntensity
                            )
                        },
                        onDrawSurface = { drawRect(containerColor) }
                    )
                } else {
                    Modifier.background(containerColor, CircleShape)
                }
            )
            .height(GlassTokens.glassSearchHeight)
            .fillMaxWidth(),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            Box(Modifier.weight(1f)) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { onSubmit() }),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                )
                if (value.isEmpty()) {
                    Text(
                        placeholder,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 15.sp
                    )
                }
            }
            if (value.isNotEmpty()) {
                IconButton(
                    onClick = { onValueChange("") },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "清除",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
