package com.limao.liquidbrowser.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.limao.liquidbrowser.features.bookmarks.BookmarksScreen
import com.limao.liquidbrowser.features.browser.BrowserScreen
import com.limao.liquidbrowser.features.history.HistoryScreen
import com.limao.liquidbrowser.features.home.HomeScreen
import com.limao.liquidbrowser.features.settings.SettingsScreen
import com.limao.liquidbrowser.features.settings.ThemeMode
import com.limao.liquidbrowser.ui.components.AppTopBar
import com.limao.liquidbrowser.ui.components.LiquidBottomBar
import com.limao.liquidbrowser.ui.theme.LiquidBrowserTheme

/** 应用级导航。状态驱动，不引入 Navigation 组件，阶段 2+ 需要时再评估。 */
enum class Screen { Home, Browser, Bookmarks, History, Settings }

@Composable
fun LiquidBrowserApp() {
    var screen by rememberSaveable { mutableStateOf(Screen.Home) }
    var themeMode by rememberSaveable { mutableStateOf(ThemeMode.System) }
    var glassIntensity by rememberSaveable { mutableStateOf(0.7f) }
    // 首页输入的地址/关键词，进入浏览器界面时使用。由 WebView 真正加载。
    var pendingQuery by rememberSaveable { mutableStateOf("") }

    // Backdrop 玻璃的采样源：Scaffold 内容挂 layerBackdrop，底栏/搜索框采样它。
    val backdrop = rememberLayerBackdrop()

    fun openQuery(query: String) {
        pendingQuery = query
        screen = Screen.Browser
    }

    // 系统返回键：非首页时回到首页；首页时走默认行为（退出应用）。
    // 浏览器界面有网页历史时，由 BrowserScreen 内层的 BackHandler 先回退网页。
    BackHandler(enabled = screen != Screen.Home) { screen = Screen.Home }

    LiquidBrowserTheme(themeMode = themeMode) {
        Box(Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.layerBackdrop(backdrop),
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
            ) { innerPadding ->
                // 底部留 96dp，避开悬浮胶囊底栏（64dp + 12dp 边距 + 手势导航区）。
                val contentModifier = Modifier.padding(innerPadding).padding(bottom = 96.dp)
                when (screen) {
                    Screen.Home -> HomeScreen(
                        modifier = contentModifier,
                        onOpenQuery = ::openQuery,
                        backdrop = backdrop,
                        glassIntensity = glassIntensity,
                    )
                    Screen.Browser -> BrowserScreen(
                        modifier = contentModifier,
                        query = pendingQuery,
                        onBackHome = { screen = Screen.Home },
                        backdrop = backdrop,
                        glassIntensity = glassIntensity,
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
            // KSU 风格悬浮胶囊底栏：覆盖在内容上方，浏览器界面无选中态。
            LiquidBottomBar(
                current = if (screen == Screen.Browser) null else screen,
                onSelect = { screen = it },
                backdrop = backdrop,
                glassIntensity = glassIntensity,
                modifier = Modifier.align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                    .navigationBarsPadding(),
            )
        }
    }
}
