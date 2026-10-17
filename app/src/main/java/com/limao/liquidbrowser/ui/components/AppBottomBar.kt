package com.limao.liquidbrowser.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.limao.liquidbrowser.app.Screen

/** 底部导航：首页 / 书签 / 历史 / 设置。浏览器界面由首页搜索进入，不占底部栏。 */
@Composable
fun AppBottomBar(current: Screen, onSelect: (Screen) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = current == Screen.Home,
            onClick = { onSelect(Screen.Home) },
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text("首页") },
        )
        NavigationBarItem(
            selected = current == Screen.Bookmarks,
            onClick = { onSelect(Screen.Bookmarks) },
            icon = { Icon(Icons.Filled.Bookmarks, contentDescription = null) },
            label = { Text("书签") },
        )
        NavigationBarItem(
            selected = current == Screen.History,
            onClick = { onSelect(Screen.History) },
            icon = { Icon(Icons.Filled.History, contentDescription = null) },
            label = { Text("历史") },
        )
        NavigationBarItem(
            selected = current == Screen.Settings,
            onClick = { onSelect(Screen.Settings) },
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            label = { Text("设置") },
        )
    }
}
