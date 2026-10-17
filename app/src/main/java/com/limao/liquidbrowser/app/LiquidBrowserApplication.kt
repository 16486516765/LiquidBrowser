package com.limao.liquidbrowser.app

import android.app.Application

/** 应用入口：最早时机安装崩溃日志，覆盖 MainActivity 之前发生的崩溃。 */
class LiquidBrowserApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashLogger.install(this)
    }
}
