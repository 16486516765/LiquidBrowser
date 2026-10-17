package com.limao.liquidbrowser.ui.theme

import androidx.compose.ui.unit.dp

/**
 * 设计令牌（提案 §三.4）：颜色、圆角、透明度、间距统一放这里，禁止各页面散落硬编码。
 *
 * 阶段 1：骨架只用半透明材质 + 细边框 + 柔和阴影做“轻玻璃”占位，
 * 不引入 Backdrop 库。阶段 3 按已验证的 Backdrop API 接入真实折射，
 * 到时这些令牌直接映射为 drawBackdrop 的 shape / effects / surface 参数。
 */
object GlassTokens {
    /** 玻璃卡片 / 工具栏圆角 */
    val cardCornerRadius = 20.dp
    val barCornerRadius = 28.dp
    val chipCornerRadius = 999.dp

    /** 半透明表面透明度（阶段 1 占位值，阶段 3 由 Backdrop 的 onDrawSurface 接管） */
    const val surfaceAlphaLight = 0.55f
    const val surfaceAlphaDark = 0.45f

    /** 细边框宽度（dp 数值，供阶段 3 的描边参考） */
    val borderWidth = 1.dp

    /** 柔和阴影 */
    val cardElevation = 6.dp

    /** 动画：短时长、低位移（提案 §三.3） */
    const val animDurationShort = 150
    const val animDurationMedium = 250

    /** 触控目标：主要按钮接近 48dp（提案 §四/§五） */
    val touchTarget = 48.dp

    /** 页面边距 */
    val pagePadding = 20.dp
    val cardSpacing = 12.dp
}
