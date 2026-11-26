package com.lamah.studio

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private var fileCallback: ValueCallback<Array<Uri>>? = null

    private val filePicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val callback = fileCallback
        fileCallback = null
        val data = result.data
        val uris = when {
            result.resultCode != RESULT_OK -> null
            data?.clipData != null -> {
                val clip = data.clipData!!
                Array(clip.itemCount) { index -> clip.getItemAt(index).uri }
            }
            data?.data != null -> arrayOf(data.data!!)
            else -> null
        }
        callback?.onReceiveValue(uris)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(16, 17, 20)
        window.navigationBarColor = android.graphics.Color.rgb(16, 17, 20)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        webView = WebView(this).apply {
            setBackgroundColor(android.graphics.Color.rgb(16, 17, 20))
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.mediaPlaybackRequiresUserGesture = true
            settings.javaScriptCanOpenWindowsAutomatically = false
            settings.setSupportMultipleWindows(false)
            CookieManager.getInstance().setAcceptCookie(false)

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val uri = request?.url ?: return false
                    return uri.scheme != "file" && uri.scheme != "about"
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onShowFileChooser(
                    webView: WebView?,
                    callback: ValueCallback<Array<Uri>>?,
                    params: FileChooserParams?
                ): Boolean {
                    fileCallback?.onReceiveValue(null)
                    fileCallback = callback
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "*/*"
                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
                        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    }
                    filePicker.launch(intent)
                    return true
                }
            }
        }

        setContentView(webView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))

        if (savedInstanceState == null) {
            webView.loadUrl("file:///android_asset/index.html")
        } else {
            webView.restoreState(savedInstanceState)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        fileCallback?.onReceiveValue(null)
        webView.destroy()
        super.onDestroy()
    }
}
