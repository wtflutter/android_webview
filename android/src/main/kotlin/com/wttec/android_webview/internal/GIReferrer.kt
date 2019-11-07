package com.wttec.android_webview.internal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.facebook.CampaignTrackingReceiver
import com.wttec.android_webview.AndroidWebviewPlugin

/**
 * Date:       2019-05-28
 * Author:     Su Xing
 * Describe:   utm_source
 */
class GIReferrer : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val referrer = intent.getStringExtra("referrer") ?: ""
        val uuid = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val map = HashMap<String, Any>()
        map["uuid"] = uuid
        map["topic"] = referrer
        AndroidWebviewPlugin.channel?.invokeMethod("saveTopic", map)
        CampaignTrackingReceiver().onReceive(context, intent)
    }
}
