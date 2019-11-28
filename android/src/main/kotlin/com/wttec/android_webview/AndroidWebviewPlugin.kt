/*
BSD 2-Clause License

Copyright (c) 2019, wtflutter
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.wttec.android_webview

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.text.TextUtils
import android.util.Log
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.wttec.android_webview.internal.DownloadIntentService
import com.wttec.android_webview.internal.MessageEvent
import com.wttec.android_webview.ui.WebActivity
import com.wttec.android_webview.utils.*
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

class AndroidWebviewPlugin(var activity: Activity) : MethodCallHandler {

    companion object {
        var channel: MethodChannel? = null
        var eventSink: EventChannel.EventSink? = null
        var mainClass: Class<out Activity>? = null
        var webView: WebView? = null

        @JvmStatic
        fun registerWith(registrar: Registrar) {
            channel = MethodChannel(registrar.messenger(), "com.wttec.android_webview")
            EventChannel(registrar.messenger(), "com.wttec.android_webview/event")
                    .setStreamHandler(object : EventChannel.StreamHandler {
                        override fun onListen(p0: Any?, p1: EventChannel.EventSink?) {
                            Log.e("plugin", "listen....")
                            eventSink = p1
                        }

                        override fun onCancel(p0: Any?) {

                        }

                    })
            mainClass = registrar.activity().javaClass
            webView = WebView(registrar.activeContext())
            initWebView()
            val plugin = AndroidWebviewPlugin(registrar.activity())
            plugin.register()
            channel?.setMethodCallHandler(plugin)
        }

        private fun initWebView() {
            webView?.settings?.javaScriptEnabled = true
            webView?.run {
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        webView?.loadUrl(url)
                        return super.shouldOverrideUrlLoading(view, url)
                    }

                    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                        super.onReceivedSslError(view, handler, error)
                        handler?.proceed()
                    }
                }
            }
            webView?.setDownloadListener { url, _, _, _, _ ->
                downloadAPK(webView?.context, url)
            }
        }

        private fun downloadAPK(context: Context?, downloadUrl: String) {
            val permissionGroup = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            context?.checkPermissions(permissionGroup) {
                if (it) {
                    val title = webView?.getTag(R.id.web_name) as String? ?: ""
                    val id = webView?.tag as Int? ?: 0
                    DownloadIntentService.openMe2(context, downloadUrl, title, id)
                } else {
                    Toast.makeText(context.applicationContext, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }

        @JvmStatic
        fun checkIntent(data: Intent?, activity: Activity) {
            val url = data?.getStringExtra("openUrl")
            if (TextUtils.isEmpty(url)) return
            channel?.invokeMethod("saveEventTracking", 2)
            WebActivity.openMe(activity, url ?: "")
        }
    }

    private fun register() {
        EventBus.getDefault().register(this)
    }


    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
            "toWeb" -> toWebAction(call)
            "version" -> result.success(activity.versionName())
            "token" -> activity.getToken {
                //获取token参数
                activity.initPush()
                result.success(it)
            }
            "openPicture" -> PictureUtil.openPicture(activity) {
                result.success(it)
            }
            "isDownloaded" -> {
                val id = call.arguments as Int
                val file = File(FileUtil.getDownloadPath() + FileUtil.getFileName(activity, id))
                result.success(file.exists())
            }
            "install" -> {
                val id = call.arguments as Int
                val file = File(FileUtil.getDownloadPath() + FileUtil.getFileName(activity, id))
                FileUtil.installAPk(activity, file)
            }
            "encrypt" -> {
                val map = call.arguments as Map<*, *>
                val key = map["key"] as String
                val data = map["data"] as String
                result.success(AesUtil.encode(key, data))
            }
            "decrypt" -> {
                val map = call.arguments as Map<*, *>
                val key = map["key"] as String
                val data = map["data"] as String
                result.success(AesUtil.decode(key, data))
            }
            else -> result.notImplemented()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        val map = HashMap<String, Any>()
        map["id"] = event.id
        map["progress"] = event.progress
        eventSink?.success(map)
    }

    private fun toWebAction(methodCall: MethodCall) {
        val map = methodCall.arguments as Map<*, *>
        val openType = map["openType"] as Int
        val androidUrl = map["url"] as String
        val id = map["id"] as Int
        val name = map["name"] as String
        val params = HashMap<String, Any>()
        params["tag"] = openType
        params["field1"] = androidUrl
        when (openType) {
            4 -> {
                webView?.tag = id
                webView?.setTag(R.id.web_name, name)
                if (androidUrl.endsWith(".apk")) {
                    downloadAPK(activity, androidUrl)
                } else {
                    EventBus.getDefault().post(MessageEvent(id, 0))
                    webView?.loadUrl(androidUrl)
                }
            }
            3 -> {
                params["field3"] = if (openGp(androidUrl)) "success" else "failed"
                channel?.invokeMethod("saveEventGooglePlay", params)
            }
            2 -> {
                try {
                    val parse = Uri.parse(androidUrl)
                    val intent = Intent(Intent.ACTION_VIEW, parse)
                    activity.startActivity(intent)
                    params["field3"] = "success"
                } catch (e: Exception) {
                    params["field3"] = "failed"
                }
                channel?.invokeMethod("saveEventGooglePlay", params)
            }
            else -> WebActivity.openMe(activity, androidUrl)
        }
    }

    fun openGp(storeUrl: String): Boolean {
        try {
            if (TextUtils.isEmpty(storeUrl)) return false;
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(storeUrl)
                setPackage("com.android.vending")
            }
            activity.startActivity(intent)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            WebActivity.openMe(activity, storeUrl)
        }
        return false
    }


}
