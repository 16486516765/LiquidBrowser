package com.limao.liquidbrowser.ui.theme

import androidx.compose.ui.unit.dp

/**
 * 设计令牌（提案 §三.4）：颜色、圆角、透明度、间距统一放这里，禁止各页面散落硬编码。
 *
 * 液态玻璃已接入 Kyant0/AndroidLiquidGlass（Backdrop 2.0.1，Apache 2.0）。
 * 下面的 Backdrop 参数映射为 drawBackdrop 的 effects / onDrawSurface，取 iOS 风格：
 * 高透明（表面 alpha 0.4）、轻折射（24dp）、柔和高光。设置里的玻璃强度滑杆
 * 按比例缩放 blur / 折射，设为 0 时退回纯半透明材质。
 */
object GlassTokens {
    /** 玻璃卡片 / 工具栏圆角 */
    val cardCornerRadius = 20.dp
    val barCornerRadius = 28.dp
    val chipCornerRadius = 999.dp

    /** 半透明表面透明度（非 Backdrop 组件的占位值） */
    const val surfaceAlphaLight = 0.55f
    const val surfaceAlphaDark = 0.45f

    /** Backdrop 玻璃表面透明度（iOS 风格高透明） */
    const val glassSurfaceAlpha = 0.4f

    /** Backdrop 模糊半径 / 折射高度 / 折射量（强度 1.0 时的值） */
    val glassBlurRadius = 8.dp
    val glassRefractionHeight = 24.dp
    val glassRefractionAmount = 24.dp

    /** 底栏 / 指示 pill / 搜索框高度（KSU / Kyant0 官方示例同款尺寸） */
    val glassBarHeight = 64.dp
    val glassIndicatorHeight = 56.dp
    val glassSearchHeight = 56.dp

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
