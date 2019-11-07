package com.wttec.android_webview.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.v4.app.NotificationManagerCompat
import android.view.LayoutInflater
import com.wttec.android_webview.R
import kotlinx.android.synthetic.main.fw_dialog_notification.view.*

object NotificationUtil {
    /**
     * 检查通知是否打开
     *
     * @param context
     */
    fun checkNotification(context: Context) {
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        showNotificationDialog(context)
    }


    private fun showNotificationDialog(context: Context) {
        val v = LayoutInflater.from(context).inflate(R.layout.fw_dialog_notification, null)
        val dialog = Dialog(context, R.style.ConfirmDialog)
        dialog.setContentView(v)
        v.tv_ok.setOnClickListener {
            toOpenNotification(context)
            dialog.dismiss()
        }
        v.tv_cancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun toOpenNotification(context: Context) {
        val intent = Intent()
        val packageName = context.packageName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS

            intent.putExtra("app_package", packageName)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            intent.putExtra("app_uid", context.applicationInfo.uid)
            if (isIntentAvailable(context, intent)) {
                context.startActivity(intent)
                return
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
        intent.data = Uri.fromParts("package", packageName, null)
        context.startActivity(intent)
    }

    @SuppressLint("WrongConstant")
    private fun isIntentAvailable(context: Context, intent: Intent): Boolean {
        val pm = context.packageManager
        val list = pm.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES)
        return list.size > 0
    }
}
