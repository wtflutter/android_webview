package com.wttec.android_webview.utils

import android.Manifest
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.google.gson.Gson
import java.lang.ref.WeakReference

class JsObject(private var webViewRef: WeakReference<WebView>) {

    @JavascriptInterface
    fun _getContact() {
        webViewRef.get()?.also { webView ->
            val context = webView.context
            webView.post {
                context.checkPermission(Manifest.permission.READ_CONTACTS) {
                    val str = if (it) Gson().toJson(ContactsUtil.getContacts(context)) else "null"
                    val script = "javascript:(function(){window.wtJsBridge.contactCallback($str)})();"
                    webView.loadUrl(script)
                }
            }
        }
    }
}