package com.limao.liquidbrowser.features.browser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kyant.backdrop.Backdrop
import com.limao.liquidbrowser.ui.components.GlassSearchBar
import java.net.URLEncoder

/** 输入是网址还是搜索词：无空格且含点（或带 http 前缀）按网址处理，否则按搜索词。 */
private fun isProbablyUrl(input: String): Boolean {
    if (input.contains(" ")) return false
    if (input.startsWith("http://") || input.startsWith("https://")) return true
    return input.contains(".")
}

private fun queryToUrl(input: String): String {
    val q = input.trim()
    return if (isProbablyUrl(q)) {
        if (q.startsWith("http://") || q.startsWith("https://")) q else "https://$q"
    } else {
        // 默认搜索引擎用 Bing（大陆无需 VPN 可用）。
        "https://www.bing.com/search?q=" + URLEncoder.encode(q, "UTF-8")
    }
}

/**
 * 浏览器界面：WebView + 胶囊玻璃地址栏 + 进度条 + 后退/前进/刷新。
 * 系统返回键优先回退网页历史，无历史时由外层 BackHandler 回首页。
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(
    modifier: Modifier = Modifier,
    query: String,
    onBackHome: () -> Unit,
    backdrop: Backdrop,
    glassIntensity: Float = 1f,
) {
    var urlText by remember(query) { mutableStateOf(query) }
    var progress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(false) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                    isLoading = true
                    loadError = null
                    if (url != null) urlText = url
                }

                override fun onPageFinished(view: WebView, url: String?) {
                    isLoading = false
                    progress = 1f
                    canGoBack = view.canGoBack()
                    canGoForward = view.canGoForward()
                }

                override fun onReceivedError(
                    view: WebView,
                    request: WebResourceRequest,
                    error: WebResourceError
                ) {
                    if (request.isForMainFrame) {
                        isLoading = false
                        loadError = error.description?.toString() ?: "加载失败"
                    }
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    progress = newProgress / 100f
                }
            }
        }
    }

    fun loadQuery(q: String) {
        val trimmed = q.trim()
        if (trimmed.isEmpty()) return
        loadError = null
        webView.loadUrl(queryToUrl(trimmed))
    }

    LaunchedEffect(query) {
        if (query.isNotBlank()) loadQuery(query)
    }

    BackHandler(enabled = canGoBack) { webView.goBack() }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, webView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> webView.onResume()
                Lifecycle.Event.ON_PAUSE -> webView.onPause()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            webView.destroy()
        }
    }

    Column(modifier.fillMaxSize()) {
        GlassSearchBar(
            value = urlText,
            onValueChange = { urlText = it },
            onSubmit = { loadQuery(urlText) },
            backdrop = backdrop,
            glassIntensity = glassIntensity,
            placeholder = "输入网址或搜索关键词",
        )
        if (isLoading) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(2.dp),
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { if (webView.canGoBack()) webView.goBack() },
                enabled = canGoBack,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "后退")
            }
            IconButton(
                onClick = { if (webView.canGoForward()) webView.goForward() },
                enabled = canGoForward,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "前进")
            }
            IconButton(onClick = { webView.reload() }) {
                Icon(Icons.Filled.Refresh, contentDescription = "刷新")
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onBackHome) { Text("首页") }
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (query.isBlank()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "在上方输入网址或关键词开始浏览",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                AndroidView(
                    factory = { webView },
                    modifier = Modifier.fillMaxSize(),
                )
                if (loadError != null) {
                    Box(
                        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "加载失败：$loadError",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
