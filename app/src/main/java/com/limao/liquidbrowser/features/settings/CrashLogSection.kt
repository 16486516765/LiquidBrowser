package com.limao.liquidbrowser.features.settings

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limao.liquidbrowser.app.CrashLogger
import java.io.File

/** 设置页里的崩溃日志区：查看 / 分享 / 清除。日志由 CrashLogger 在闪退时写入外部存储。 */
@Composable
fun CrashLogSection() {
    val context = LocalContext.current
    var crashLogs by remember { mutableStateOf(CrashLogger.getCrashLogs(context)) }
    var viewingFile by remember { mutableStateOf<File?>(null) }
    var viewingText by remember { mutableStateOf<String?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }

    fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }package com.limao.liquidbrowser.features.settings

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limao.liquidbrowser.app.CrashLogger
import java.io.File

/** 设置页里的崩溃日志区：查看 / 分享 / 清除。日志由 CrashLogger 在闪退时写入外部存储。 */
@Composable
fun CrashLogSection() {
    val context = LocalContext.current
    var crashLogs by remember { mutableStateOf(CrashLogger.getCrashLogs(context)) }
    var viewingFile by remember { mutableStateOf<File?>(null) }
    var viewingText by remember { mutableStateOf<String?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }

    fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "分享崩溃日志"))
    }

    fun exportLogcat() {
        val text = runCatching {
            val proc = Runtime.getRuntime().exec(
                arrayOf("logcat", "-d", "-t", "1000", "--pid=${android.os.Process.myPid()}")
            )
            proc.inputStream.bufferedReader().readText()
        }.getOrDefault("读取 logcat 失败")
        shareText(text)
    }

    Card(Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("崩溃日志", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            if (crashLogs.isEmpty()) {
                Text(
                    "暂无崩溃日志",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    "共 ${crashLogs.size} 条日志",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                crashLogs.forEach { file ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(file.name, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        TextButton(onClick = {
                            viewingFile = file
                            viewingText = runCatching { file.readText() }.getOrDefault("读取失败")
                        }) { Text("查看") }
                        TextButton(onClick = {
                            shareText(runCatching { file.readText() }.getOrDefault(""))
                        }) { Text("分享") }
                    }
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { showClearConfirm = true }) { Text("清除全部日志") }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "闪退后日志自动保存到：Android/data/com.limao.liquidbrowser/files/crash_logs/，" +
                    "用文件管理器也能直接打开。",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { throw RuntimeException("测试崩溃日志") }) { Text("测试崩溃（验证日志是否生效）") }
            TextButton(onClick = { exportLogcat() }) { Text("导出 logcat（抓 native 闪退）") }
            Text(
                "点“测试崩溃”会故意让 App 闪退一次，重开后这里应出现一条测试日志。如果测试日志能出来，说明日志功能正常，原来的闪退可能是 native 崩溃，用“导出 logcat”抓。",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }

    if (viewingText != null) {
        AlertDialog(
            onDismissRequest = { viewingText = null; viewingFile = null },
            title = { Text(viewingFile?.name ?: "崩溃日志") },
            text = {
                Text(
                    viewingText!!,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            },
            confirmButton = {
                TextButton(onClick = { viewingText = null; viewingFile = null }) { Text("关闭") }
            },
            dismissButton = {
                TextButton(onClick = { viewingText?.let { shareText(it) } }) { Text("分享") }
            }
        )
    }
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("清除全部日志？") },
            text = { Text("将删除全部 ${crashLogs.size} 条崩溃日志。") },
            confirmButton = {
                TextButton(onClick = {
                    CrashLogger.clearLogs(context)
                    crashLogs = emptyList()
                    showClearConfirm = false
                }) { Text("清除") }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("取消") }
            }
        )
    }
}

        context.startActivity(Intent.createChooser(intent, "分享崩溃日志"))
    }

    Card(Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("崩溃日志", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            if (crashLogs.isEmpty()) {
                Text(
                    "暂无崩溃日志",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    "共 ${crashLogs.size} 条日志",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                crashLogs.forEach { file ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(file.name, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        TextButton(onClick = {
                            viewingFile = file
                            viewingText = runCatching { file.readText() }.getOrDefault("读取失败")
                        }) { Text("查看") }
                        TextButton(onClick = {
                            shareText(runCatching { file.readText() }.getOrDefault(""))
                        }) { Text("分享") }
                    }
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { showClearConfirm = true }) { Text("清除全部日志") }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "闪退后日志自动保存到：Android/data/com.limao.liquidbrowser/files/crash_logs/，" +
                    "用文件管理器也能直接打开。",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }

    if (viewingText != null) {
        AlertDialog(
            onDismissRequest = { viewingText = null; viewingFile = null },
            title = { Text(viewingFile?.name ?: "崩溃日志") },
            text = {
                Text(
                    viewingText!!,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            },
            confirmButton = {
                TextButton(onClick = { viewingText = null; viewingFile = null }) { Text("关闭") }
            },
            dismissButton = {
                TextButton(onClick = { viewingText?.let { shareText(it) } }) { Text("分享") }
            }
        )
    }
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("清除全部日志？") },
            text = { Text("将删除全部 ${crashLogs.size} 条崩溃日志。") },
            confirmButton = {
                TextButton(onClick = {
                    CrashLogger.clearLogs(context)
                    crashLogs = emptyList()
                    showClearConfirm = false
                }) { Text("清除") }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("取消") }
            }
        )
    }
}
