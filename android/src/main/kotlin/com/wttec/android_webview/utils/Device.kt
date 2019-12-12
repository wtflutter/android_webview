package com.wttec.android_webview.utils

import android.content.Context
import android.os.Build
import android.provider.Settings

fun Context.deviceParams():Map<String,Any> {
    val hashMap = HashMap<String, Any>()
    val uuid = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    hashMap["uuid"] = uuid
    hashMap["mobile_operator"] = 4
    hashMap["wifi_name"] = CommonUtil.getWifiName(this)
    hashMap["network_state"] = CommonUtil.getNetType(this)
    hashMap["jailbreak_status"] = CommonUtil.getDeviceRootedState()
    hashMap["crack_status"] = CommonUtil.getDeviceRootedState()
    hashMap["device_type"] = Build.BRAND
    hashMap["system_version"] = Build.VERSION.RELEASE
    hashMap["app_version"] = versionName()
    hashMap["js_version"] = versionName()
    hashMap["opportunity"] = 1
    hashMap["device_name"] = Build.MODEL
    return hashMap
}

