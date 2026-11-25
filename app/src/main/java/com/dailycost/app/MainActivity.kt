package com.dailycost.app

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import com.dailycost.app.data.AppStore
import java.io.File

/**
 * 容器：WebView 承载前端界面，Kotlin 负责数据持久化与系统能力。
 */
class MainActivity : ComponentActivity() {

    private lateinit var web: WebView
    private lateinit var store: AppStore
    private var fileCallback: ValueCallback<Array<Uri>>? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
        store = AppStore.get(applicationContext)

        web = WebView(this)
        web.setBackgroundColor(Color.parseColor("#F2F4F7"))
        with(web.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            textZoom = 100
            mediaPlaybackRequiresUserGesture = false
            setSupportZoom(false)
            builtInZoomControls = false
            @Suppress("DEPRECATION")
            allowFileAccessFromFileURLs = true
            @Suppress("DEPRECATION")
            allowUniversalAccessFromFileURLs = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                safeBrowsingEnabled = false
            }
        }
        web.addJavascriptInterface(Bridge(), "Android")
        web.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(message: android.webkit.ConsoleMessage?): Boolean {
                android.util.Log.e("WV", "${message?.message()} @${message?.sourceId()}:${message?.lineNumber()}")
                return true
            }
            override fun onShowFileChooser(
                webView: WebView?,
                callback: ValueCallback<Array<Uri>>?,
                params: FileChooserParams?
            ): Boolean {
                fileCallback?.onReceiveValue(null)
                fileCallback = callback
                return try {
                    val intent = params?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "*/*"
                    }
                    startActivityForResult(intent, REQ_FILE)
                    true
                } catch (e: Exception) {
                    fileCallback = null
                    false
                }
            }
        }

        // 首次进入用当前设置决定状态栏配色，避免闪白
        val dark = when (store.settings.value.darkMode) {
            2 -> true
            1 -> false
            else -> (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
        }
        applySystemBars(dark)

        setContentView(web)
        web.webViewClient = object : android.webkit.WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                // 首帧就绪：如需调试可在此打日志
            }
        }
        web.loadUrl("file:///android_asset/www/index.html")

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                web.evaluateJavascript("window.hardwareBack && window.hardwareBack()") { }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQ_FILE) {
            val cb = fileCallback
            fileCallback = null
            cb?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data))
            return
        }
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun applySystemBars(dark: Boolean) {
        val bg = if (dark) "#080B11" else "#F2F4F7"
        window.statusBarColor = Color.parseColor(bg)
        window.navigationBarColor = Color.parseColor(bg)
        val flags = window.decorView.systemUiVisibility
        window.decorView.systemUiVisibility = if (dark) {
            flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() and
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        } else {
            flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
        web.setBackgroundColor(Color.parseColor(bg))
    }

    inner class Bridge {
        @JavascriptInterface
        fun getState(): String = store.stateJson()

        @JavascriptInterface
        fun saveState(json: String) {
            runCatching { store.saveStateFromWeb(json) }
        }

        @JavascriptInterface
        fun setSystemBars(dark: Boolean) {
            runOnUiThread { applySystemBars(dark) }
        }

        @JavascriptInterface
        fun finishApp() {
            runOnUiThread { finish() }
        }

        /** 写入缓存目录后唤起系统分享，用户可保存到文件/微信等 */
        @JavascriptInterface
        fun saveFile(name: String, mime: String, content: String) {
            runOnUiThread {
                runCatching {
                    val dir = File(cacheDir, "export").apply { mkdirs() }
                    val file = File(dir, name)
                    file.writeText(content)
                    val uri = FileProvider.getUriForFile(
                        this@MainActivity,
                        "$packageName.fileprovider",
                        file
                    )
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = mime
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(send, "保存或分享"))
                }.onFailure {
                    Toast.makeText(this@MainActivity, "导出失败：${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        @JavascriptInterface
        fun toast(msg: String) {
            runOnUiThread { Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show() }
        }
    }

    companion object {
        private const val REQ_FILE = 1001
    }
}
