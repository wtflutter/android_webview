package com.wttec.android_webview.internal

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.v4.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.wttec.android_webview.AndroidWebviewPlugin
import com.wttec.android_webview.R
import java.util.*


class FCMService : FirebaseMessagingService() {
    override fun onNewToken(s: String?) {
        super.onNewToken(s)

    }

    override fun onMessageReceived(msg: RemoteMessage?) {
        super.onMessageReceived(msg)
        if (msg != null)
            showNotify(msg)
    }

    private val NOTIFICATION_ID = UUID.randomUUID().hashCode()
    private val main = Handler(Looper.getMainLooper())
    private fun showNotify(msg: RemoteMessage) {
        main.post {
            AndroidWebviewPlugin.channel?.invokeMethod("saveEventTracking", 1)
        }
        val ticker = msg.notification?.title ?: ""
        val title = msg.notification?.title ?: ""
        val content = msg.notification?.body ?: ""
        val channelId = packageName

        val mNotifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelId, importance)
            channel.description = ticker
            channel.enableLights(true)
            mNotifyManager.createNotificationChannel(channel)
        }
        val mBuilder = NotificationCompat.Builder(this, "download")
        mBuilder.setContentTitle(title)
                .setContentText(content)
                //通知首次出现在通知栏，带上升动画效果的
                .setTicker(ticker)
                .setDefaults(Notification.DEFAULT_ALL)
                .setChannelId(channelId)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setSmallIcon(R.drawable.ic_notify_logo)
            mBuilder.color = resources.getColor(R.color.notifyColor)
        } else {
            mBuilder.setSmallIcon(applicationInfo.icon)
        }
        val intent = Intent(this, AndroidWebviewPlugin.mainClass)
        intent.putExtra("openUrl", msg.data["openUrl"] ?: "")

        val pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent
                .FLAG_UPDATE_CURRENT
        )
        mBuilder.setContentIntent(pendingIntent)
        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build())
    }
}
