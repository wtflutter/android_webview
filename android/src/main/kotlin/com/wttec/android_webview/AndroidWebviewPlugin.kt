package com.wttec.android_webview

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import com.wttec.android_webview.ui.WebActivity
import com.wttec.android_webview.utils.*
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

class AndroidWebviewPlugin(var activity: Activity) : MethodCallHandler {

    companion object {
        var channel: MethodChannel? = null
        var mainClass: Class<out Activity>? = null
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            channel = MethodChannel(registrar.messenger(), "com.wttec.android_webview")
            mainClass = registrar.activity().javaClass
            channel?.setMethodCallHandler(AndroidWebviewPlugin(registrar.activity()))
        }

        @JvmStatic
        fun checkIntent(data: Intent?, activity: Activity) {
            val url = data?.getStringExtra("openUrl")
            if (TextUtils.isEmpty(url)) return
            channel?.invokeMethod("saveEventTracking", 2)
            WebActivity.openMe(activity, url ?: "")
        }
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
            "login" -> FirebaseUtil.login(activity) {
                result.success(it)
            }
            "logout" -> FirebaseUtil.logout()
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
            else -> result.notImplemented()
        }
    }

    private fun toWebAction(methodCall: MethodCall) {
        val map = methodCall.arguments as Map<*, *>
        val openType = map["openType"] as Int
        val androidUrl = map["url"] as String
        val params = HashMap<String, Any>()
        params["tag"] = openType
        params["field1"] = androidUrl
        when (openType) {
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
            //打开浏览器
            WebActivity.openMe(activity, storeUrl)
        }
        return false
    }


}
