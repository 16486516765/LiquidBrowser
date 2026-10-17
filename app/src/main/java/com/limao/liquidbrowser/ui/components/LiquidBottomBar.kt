// Bottom bar adapted from Kyant0/AndroidLiquidGlass catalog LiquidBottomTabs (Apache 2.0).
// https://github.com/Kyant0/AndroidLiquidGlass
// Floating capsule pill + spring indicator + drag-to-switch + press scale, rendered with
// Kyant0 Backdrop (vibrancy + blur + lens + highlight). Same look/behavior family as
// KSU's FloatingBottomBar, implemented on the Backdrop API.
//
// NOTE: Backdrop only samples Compose layers. When this bar floats over the WebView
// (Browser screen), it cannot refract the web page (WebView is a native view, not a
// Compose layer). It still shows tint + highlight + border, but the refraction is of
// the Compose background underneath, not the page. See README known limitations.
package com.limao.liquidbrowser.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.limao.liquidbrowser.app.Screen
import com.limao.liquidbrowser.ui.components.liquid.DampedDragAnimation
import com.limao.liquidbrowser.ui.components.liquid.InteractiveHighlight
import com.limao.liquidbrowser.ui.theme.GlassTokens
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

private data class BottomTab(val screen: Screen, val icon: ImageVector, val label: String)

private val Tabs = listOf(
    BottomTab(Screen.Home, Icons.Filled.Home, "首页"),
    BottomTab(Screen.Bookmarks, Icons.Filled.Bookmarks, "书签"),
    BottomTab(Screen.History, Icons.Filled.History, "历史"),
    BottomTab(Screen.Settings, Icons.Filled.Settings, "设置"),
)

private val LocalLiquidBottomBarTabScale = staticCompositionLocalOf { { 1f } }

@Composable
private fun RowScope.LiquidBottomBarTab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val scale = LocalLiquidBottomBarTabScale.current
    Column(
        modifier
            .clip(CircleShape)
            .clickable(
                interactionSource = null,
                indication = null,
                role = Role.Tab,
                onClick = onClick
            )
            .fillMaxHeight()
            .weight(1f)
            .graphicsLayer {
                val scale = scale()
                scaleX = scale
                scaleY = scale
            },
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

/**
 * KSU 风格悬浮胶囊底部导航栏：64dp 胶囊 + 弹簧指示 pill + 横向拖拽切 tab + 按压缩放，
 * 用 Backdrop 做液态玻璃（vibrancy + blur + lens + 高光），透明度/折射按 iOS 风格取参。
 *
 * @param current 当前 tab；浏览器界面传 null（无选中态，指示 pill 隐藏）。
 * @param glassIntensity 玻璃强度 0..1，来自设置滑杆；0 = 关闭玻璃特效，退回纯半透明材质。
 */
@Composable
fun LiquidBottomBar(
    current: Screen?,
    onSelect: (Screen) -> Unit,
    backdrop: Backdrop,
    glassIntensity: Float = 1f,
    modifier: Modifier = Modifier,
) {
    val containerColor = MaterialTheme.colorScheme.surface.copy(alpha = GlassTokens.glassSurfaceAlpha)
    val accentColor = MaterialTheme.colorScheme.primary
    val tabContentColor = MaterialTheme.colorScheme.onSurface
    val glassEnabled = glassIntensity > 0.01f

    if (!glassEnabled) {
        LiquidBottomBarFallback(current = current, onSelect = onSelect, modifier = modifier)
        return
    }

    val tabsBackdrop = rememberLayerBackdrop()

    BoxWithConstraints(
        modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        val density = LocalDensity.current
        val tabWidth = with(density) {
            (constraints.maxWidth.toFloat() - 8.dp.toPx()) / Tabs.size
        }
        val selectedIndex = current?.let { Tabs.indexOfFirst { tab -> tab.screen == current } } ?: -1

        val offsetAnimation = remember { Animatable(0f) }
        val panelOffset by remember(density) {
            derivedStateOf {
                val fraction = (offsetAnimation.value / constraints.maxWidth).fastCoerceIn(-1f, 1f)
                with(density) {
                    4.dp.toPx() * fraction.sign * EaseOut.transform(abs(fraction))
                }
            }
        }

        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        val animationScope = rememberCoroutineScope()
        var currentIndex by remember { mutableIntStateOf(selectedIndex.coerceAtLeast(0)) }
        val dampedDragAnimation = remember(animationScope) {
            DampedDragAnimation(
                animationScope = animationScope,
                initialValue = selectedIndex.coerceAtLeast(0).toFloat(),
                valueRange = 0f..(Tabs.size - 1).toFloat(),
                visibilityThreshold = 0.001f,
                initialScale = 1f,
                pressedScale = 78f / 56f,
                onDragStarted = {},
                onDragStopped = {
                    val targetIndex = targetValue.fastRoundToInt().fastCoerceIn(0, Tabs.size - 1)
                    currentIndex = targetIndex
                    onSelect(Tabs[targetIndex].screen)
                    animateToValue(targetIndex.toFloat())
                    animationScope.launch {
                        offsetAnimation.animateTo(
                            0f,
                            spring(1f, 300f, 0.5f)
                        )
                    }
                },
                onDrag = { _, dragAmount ->
                    updateValue(
                        (targetValue + dragAmount.x / tabWidth * if (isLtr) 1f else -1f)
                            .fastCoerceIn(0f, (Tabs.size - 1).toFloat())
                    )
                    animationScope.launch {
                        offsetAnimation.snapTo(offsetAnimation.value + dragAmount.x)
                    }
                }
            )
        }
        LaunchedEffect(selectedIndex) {
            if (selectedIndex >= 0 && currentIndex != selectedIndex) {
                currentIndex = selectedIndex
                dampedDragAnimation.animateToValue(selectedIndex.toFloat())
            }
        }

        val interactiveHighlight = remember(animationScope) {
            InteractiveHighlight(
                animationScope = animationScope,
                position = { size, _ ->
                    androidx.compose.ui.geometry.Offset(
                        if (isLtr) (dampedDragAnimation.value + 0.5f) * tabWidth + panelOffset
                        else size.width - (dampedDragAnimation.value + 0.5f) * tabWidth + panelOffset,
                        size.height / 2f
                    )
                }
            )
        }

        val combinedBackdrop = rememberCombinedBackdrop(backdrop, tabsBackdrop)

        // 可见行：四个 tab（图标 + 文字）
        Row(
            Modifier
                .graphicsLayer {
                    translationX = panelOffset
                }
                .drawBackdrop(
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
                    layerBlock = {
                        val progress = dampedDragAnimation.pressProgress
                        val scale = lerp(1f, 1f + 16.dp.toPx() / size.width, progress)
                        scaleX = scale
                        scaleY = scale
                    },
                    onDrawSurface = { drawRect(containerColor) }
                )
                .then(interactiveHighlight.modifier)
                .height(GlassTokens.glassBarHeight)
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Tabs.forEach { tab ->
                val selected = tab.screen == current
                LiquidBottomBarTab(onClick = { onSelect(tab.screen) }) {
                    Icon(
                        tab.icon,
                        contentDescription = null,
                        tint = if (selected) tabContentColor else tabContentColor.copy(alpha = 0.6f)
                    )
                    Text(
                        tab.label,
                        fontSize = 11.sp,
                        color = if (selected) tabContentColor else tabContentColor.copy(alpha = 0.6f)
                    )
                }
            }
        }

        if (selectedIndex >= 0) {
            // 隐藏的染色行：内容被染成强调色，供指示 pill 采样，选中图标在 pill 里显强调色。
            CompositionLocalProvider(
                LocalLiquidBottomBarTabScale provides {
                    lerp(1f, 1.2f, dampedDragAnimation.pressProgress)
                }
            ) {
                Row(
                    Modifier
                        .clearAndSetSemantics {}
                        .alpha(0f)
                        .layerBackdrop(tabsBackdrop)
                        .graphicsLayer {
                            translationX = panelOffset
                        }
                        .drawBackdrop(
                            backdrop = backdrop,
                            shape = { CircleShape },
                            effects = {
                                val progress = dampedDragAnimation.pressProgress
                                vibrancy()
                                blur(GlassTokens.glassBlurRadius.toPx() * glassIntensity)
                                lens(
                                    GlassTokens.glassRefractionHeight.toPx() * glassIntensity * progress,
                                    GlassTokens.glassRefractionAmount.toPx() * glassIntensity * progress
                                )
                            },
                            highlight = {
                                val progress = dampedDragAnimation.pressProgress
                                Highlight.Default.copy(alpha = progress)
                            },
                            onDrawSurface = { drawRect(containerColor) }
                        )
                        .then(interactiveHighlight.modifier)
                        .height(GlassTokens.glassIndicatorHeight)
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .graphicsLayer(colorFilter = ColorFilter.tint(accentColor)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Tabs.forEach { tab ->
                        LiquidBottomBarTab(onClick = { onSelect(tab.screen) }) {
                            Icon(tab.icon, contentDescription = null)
                            Text(tab.label, fontSize = 11.sp)
                        }
                    }
                }
            }

            // 指示 pill：随拖拽/点击滑动，带色散折射 + 高光 + 阴影 + 内阴影。
            Box(
                Modifier
                    .padding(horizontal = 4.dp)
                    .graphicsLayer {
                        translationX =
                            if (isLtr) dampedDragAnimation.value * tabWidth + panelOffset
                            else size.width - (dampedDragAnimation.value + 1f) * tabWidth + panelOffset
                    }
                    .then(interactiveHighlight.gestureModifier)
                    .then(dampedDragAnimation.modifier)
                    .drawBackdrop(
                        backdrop = combinedBackdrop,
                        shape = { CircleShape },
                        effects = {
                            val progress = dampedDragAnimation.pressProgress
                            lens(
                                10.dp.toPx() * progress * glassIntensity,
                                14.dp.toPx() * progress * glassIntensity,
                                chromaticAberration = true
                            )
                        },
                        highlight = {
                            val progress = dampedDragAnimation.pressProgress
                            Highlight.Default.copy(alpha = progress)
                        },
                        shadow = {
                            val progress = dampedDragAnimation.pressProgress
                            Shadow(alpha = progress)
                        },
                        innerShadow = {
                            val progress = dampedDragAnimation.pressProgress
                            InnerShadow(
                                radius = 8.dp * progress,
                                alpha = progress
                            )
                        },
                        layerBlock = {
                            scaleX = dampedDragAnimation.scaleX
                            scaleY = dampedDragAnimation.scaleY
                            val velocity = dampedDragAnimation.velocity / 10f
                            scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                            scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                        },
                        onDrawSurface = {
                            val progress = dampedDragAnimation.pressProgress
                            drawRect(
                                Color.Black.copy(alpha = 0.1f),
                                alpha = 1f - progress
                            )
                            drawRect(Color.Black.copy(alpha = 0.03f * progress))
                        }
                    )
                    .height(GlassTokens.glassIndicatorHeight)
                    .fillMaxWidth(1f / Tabs.size)
            )
        }
    }
}

/** 玻璃强度为 0 时的退化形态：纯半透明胶囊 + 选中 tab 淡色底，无 Backdrop 采样。 */
@Composable
private fun LiquidBottomBarFallback(
    current: Screen?,
    onSelect: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
    Row(
        modifier
            .background(containerColor, CircleShape)
            .height(GlassTokens.glassBarHeight)
            .fillMaxWidth()
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Tabs.forEach { tab ->
            val selected = tab.screen == current
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else Color.Transparent,
                        CircleShape
                    )
                    .clickable { onSelect(tab.screen) },
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                ) {
                    Icon(
                        tab.icon,
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        tab.label,
                        fontSize = 11.sp,
                        color = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
