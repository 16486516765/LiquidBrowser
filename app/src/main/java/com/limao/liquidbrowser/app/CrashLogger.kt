package com.limao.liquidbrowser.app

import android.content.Context
import android.os.Build
import android.os.Process
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 崩溃日志：闪退时把堆栈 + 设备信息写到外部存储，设置页可查看/分享/清除。
 * 文件位置：Android/data/com.limao.liquidbrowser/files/crash_logs/crash_yyyyMMdd_HHmmss.txt
 * （外部存储，无需 root，用文件管理器就能打开；外部存储不可用时回退到内部 filesDir）
 * 最多保留 10 条，超了删最旧的。
 */
object CrashLogger {
    private const val DIR_NAME = "crash_logs"
    private const val MAX_LOGS = 10

    @Volatile
    private var installed = false

    fun install(context: Context) {
        if (installed) return
        installed = true
        val appContext = context.applicationContext
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching { writeCrashLog(appContext, throwable) }
            // 保持原有崩溃行为：交给原 handler，否则直接杀进程
            if (previousHandler != null) {
                previousHandler.uncaughtException(thread, throwable)
            } else {
                Process.killProcess(Process.myPid())
            }
        }
    }

    fun getCrashLogs(context: Context): List<File> {
        val dir = logDir(context) ?: return emptyList()
        return dir.listFiles()
            ?.filter { it.isFile && it.name.startsWith("crash_") }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    fun clearLogs(context: Context) {
        getCrashLogs(context).forEach { runCatching { it.delete() } }
    }

    private fun logDir(context: Context): File? {
        val external = context.getExternalFilesDir(null)?.let { File(it, DIR_NAME) }
        val dir = external ?: File(context.filesDir, DIR_NAME)
        return if (dir.exists() || dir.mkdirs()) dir else null
    }

    private fun writeCrashLog(context: Context, throwable: Throwable) {
        val dir = logDir(context) ?: return
        // 先清理旧日志
        dir.listFiles()
            ?.filter { it.isFile && it.name.startsWith("crash_") }
            ?.sortedByDescending { it.lastModified() }
            ?.drop(MAX_LOGS - 1)
            ?.forEach { runCatching { it.delete() } }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(dir, "crash_$timestamp.txt")
        val version = runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            "${info.versionName} (${info.versionCode})"
        }.getOrDefault("unknown")
        val header = buildString {
            appendLine("LiquidBrowser crash log")
            appendLine("time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}")
            appendLine("app version: $version")
            appendLine("device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
            appendLine("abi: ${Build.SUPPORTED_ABIS.joinToString(",")}")
            appendLine()
        }
        val stack = StringBuilder()
        var cause: Throwable? = throwable
        while (cause != null) {
            stack.append(cause.stackTraceToString())
            cause = cause.cause
            if (cause != null) stack.append("\nCaused by: ")
        }
        file.writeText(header + stack.toString())
    }
}
