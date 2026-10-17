package com.limao.liquidbrowser.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.limao.liquidbrowser.features.bookmarks.BookmarksScreen
import com.limao.liquidbrowser.features.browser.BrowserScreen
import com.limao.liquidbrowser.features.history.HistoryScreen
import com.limao.liquidbrowser.features.home.HomeScreen
import com.limao.liquidbrowser.features.settings.SettingsScreen
import com.limao.liquidbrowser.features.settings.ThemeMode
import com.limao.liquidbrowser.ui.components.AppBottomBar
import com.limao.liquidbrowser.ui.components.AppTopBar
import com.limao.liquidbrowser.ui.theme.LiquidBrowserTheme

/** 阶段 1：应用级导航。状态驱动，不引入 Navigation 组件，阶段 2+ 需要时再评估。 */
enum class Screen { Home, Browser, Bookmarks, History, Settings }

@Composable
fun LiquidBrowserApp() {
    var screen by rememberSaveable { mutableStateOf(Screen.Home) }
    var themeMode by rememberSaveable { mutableStateOf(ThemeMode.System) }
    var glassIntensity by rememberSaveable { mutableStateOf(0.7f) }
    // 首页输入的地址/关键词，进入浏览器界面时使用。阶段 2 由 WebView 真正加载。
    var pendingQuery by rememberSaveable { mutableStateOf("") }

    fun openQuery(query: String) {
        pendingQuery = query
        screen = Screen.Browser
    }

    // 系统返回键：非首页时回到首页；首页时走默认行为（退出应用）。
    // 阶段 2 接入 WebView 后，浏览器界面应先回退网页历史。
    BackHandler(enabled = screen != Screen.Home) { screen = Screen.Home }

    LiquidBrowserTheme(themeMode = themeMode) {
        Scaffold(
            topBar = {
                AppTopBar(
                    title = when (screen) {
                        Screen.Home -> "LiquidBrowser"
                        Screen.Browser -> "浏览"
                        Screen.Bookmarks -> "书签"
                        Screen.History -> "历史"
                        Screen.Settings -> "设置"
                    },
                    showBack = screen == Screen.Browser,
                    onBack = { screen = Screen.Home },
                )
            },
            bottomBar = {
                AppBottomBar(
                    current = screen,
                    onSelect = { screen = it },
                )
            },
        ) { innerPadding ->
            val contentModifier = Modifier.padding(innerPadding)
            when (screen) {
                Screen.Home -> HomeScreen(
                    modifier = contentModifier,
                    onOpenQuery = ::openQuery,
                )
                Screen.Browser -> BrowserScreen(
                    modifier = contentModifier,
                    query = pendingQuery,
                    onBackHome = { screen = Screen.Home },
                )
                Screen.Bookmarks -> BookmarksScreen(modifier = contentModifier)
                Screen.History -> HistoryScreen(modifier = contentModifier)
                Screen.Settings -> SettingsScreen(
                    modifier = contentModifier,
                    themeMode = themeMode,
                    onThemeModeChange = { themeMode = it },
                    glassIntensity = glassIntensity,
                    onGlassIntensityChange = { glassIntensity = it },
                )
            }
        }
    }
}
