package com.wttec.android_webview.internal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import com.facebook.CampaignTrackingReceiver


class GIReferrer : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val referrer = intent.getStringExtra("referrer") ?: ""
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("referrer", referrer).apply()
        CampaignTrackingReceiver().onReceive(context, intent)
    }
}
