package com.limao.liquidbrowser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.limao.liquidbrowser.app.CrashLogger
import com.limao.liquidbrowser.app.LiquidBrowserApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        CrashLogger.install(this)
        setContent { LiquidBrowserApp() }
    }
}
