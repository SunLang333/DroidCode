package com.example.maurnontech

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import MyWebServer

class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private var fileChooserCallback: ValueCallback<Array<Uri>>? = null
    private val serverPort = 8080
    private lateinit var webServer: MyWebServer

    private val fileChooserLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                fileChooserCallback?.onReceiveValue(
                    WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
                )
            } else {
                fileChooserCallback?.onReceiveValue(null)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the WebView
        webView = WebView(this)

        // Enable JavaScript and related settings
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptThirdPartyCookies(webView, true)


        // Set a WebViewClient to handle internal URL loading
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url ?: "")
                return true
            }
        }

        // Set a WebChromeClient to handle file chooser dialogs
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                fileChooserCallback = filePathCallback
                val intent = fileChooserParams?.createIntent()
                if (intent != null) {
                    fileChooserLauncher.launch(intent)
                }
                return true
            }
        }

        // Set a DownloadListener to process downloads
        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            val request = DownloadManager.Request(Uri.parse(url))
            request.setMimeType(mimeType)
            request.addRequestHeader("User-Agent", userAgent)
            request.setDescription("Downloading file...")
            request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType))
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                URLUtil.guessFileName(url, contentDisposition, mimeType)
            )
            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            Toast.makeText(applicationContext, "Downloading File", Toast.LENGTH_LONG).show()
        }

        // Set the WebView as the content view
        setContentView(webView)

        // Start the local web server to serve files from the assets folder
        webServer = MyWebServer(this, serverPort)
        webServer.start()

        // Load the front end served by the local web server
        webView.loadUrl("https://idx.google.com/")
    }
}