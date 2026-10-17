// 工具链版本与已在 GitHub Actions 上验证可构建的项目保持一致
// （AGP 9.4.1 / Kotlin 2.4.20 / Gradle 9.7.1），降低首次同步失败概率。
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
