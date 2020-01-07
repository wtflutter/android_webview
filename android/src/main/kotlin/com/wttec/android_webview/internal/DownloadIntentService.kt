package com.wttec.android_webview.internal

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Progress
import com.lzy.okserver.OkDownload
import com.lzy.okserver.download.DownloadListener
import com.wttec.android_webview.R
import com.wttec.android_webview.utils.FileUtil
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class DownloadIntentService : IntentService("download") {
    /**
     * 默认超时时间
     */
    private val DEFAULT_TIME_OUT = 10 * 1000
    /**
     * 缓存大小
     */
    private val BUFFER_SIZE = 10 * 1024

    lateinit var mNotifyManager: NotificationManager
    lateinit var mBuilder: NotificationCompat.Builder

    private val NOTIFICATION_ID = UUID.randomUUID().hashCode()

    private var mDownloadUrl = ""
    private var contentTitle = ""
    private var ticker = ""
    private var notInstall = false
    private var id = ""
    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        @JvmStatic
        fun openMe(activity: Context, downloadUrl: String, contentTitle: String, ticker: String) {
            val intent = Intent(activity, DownloadIntentService::class.java)
            intent.putExtra("downloadUrl", downloadUrl)
            intent.putExtra("contentTitle", contentTitle)
            intent.putExtra("ticker", ticker)
            activity.startService(intent)
        }

        @JvmStatic
        fun openMe2(activity: Context, downloadUrl: String, contentTitle: String, id: String) {
            val intent = Intent(activity, DownloadIntentService::class.java)
            intent.putExtra("downloadUrl", downloadUrl)
            intent.putExtra("contentTitle", contentTitle)
            intent.putExtra("ticker", "")
            intent.putExtra("id", id)
            activity.startService(intent)
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return
        mDownloadUrl = intent.getStringExtra("downloadUrl")
        contentTitle = intent.getStringExtra("contentTitle")
        ticker = intent.getStringExtra("ticker")
        id = intent.getStringExtra("id")
//        initNotify()
        startDownload2()
    }


    private fun startDownload2() {
        val request = OkGo.get<File>(mDownloadUrl)
        val tag = "$id"
        OkDownload.request(tag, request)
                .save()
                .fileName("$id.tmp")
                .register(object : DownloadListener(tag) {
                    override fun onStart(progress: Progress) {
                        updateProgress(102)
                    }

                    override fun onProgress(progress: Progress) {
                        val p = (progress.fraction * 100).toInt()
                        updateProgress(p)
                    }

                    override fun onError(progress: Progress) {
                        OkDownload.getInstance().removeTask(tag)
                        EventBus.getDefault().post(MessageEvent(id, -1))
                        deleteTmp(id)
                        clearNotify()
                    }

                    override fun onFinish(file: File, progress: Progress) {
                        OkDownload.getInstance().removeTask(tag)
                        rename(file)
                        clearNotify()
                        updateProgress(101)
                    }

                    override fun onRemove(progress: Progress) {
                        OkDownload.getInstance().removeTask(tag)
                        clearNotify()
                    }
                })
                .start()
    }

    private fun clearNotify() {
        if (::mNotifyManager.isInitialized)
            mNotifyManager.cancel(NOTIFICATION_ID)
    }

    private fun deleteTmp(id: String) {
        val path = FileUtil.getDownloadPath(this)
        val tmp = "$path$id.tmp"
        val file = File(tmp)
        if (file.exists()) file.delete()
    }


    private fun rename(old: File): File {
        val path = FileUtil.getDownloadPath(this)
        val new = File("$path$id.apk")
        if (new.exists()) new.delete()
        if (old.exists()) old.renameTo(new)
        return new
    }


    /**
     * 更新通知栏的进度(下载中)
     *
     * @param progress
     */
    private fun updateProgress(progress: Int) {
        EventBus.getDefault().post(MessageEvent(id, progress))
//        mBuilder.setContentText(String.format("%1\$d%%", progress)).setProgress(100, progress, false)
//        val pendingintent = PendingIntent.getActivity(this, 0, Intent(), PendingIntent
//                .FLAG_UPDATE_CURRENT)
//        mBuilder.setContentIntent(pendingintent)
//        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build())
    }


    private fun initNotify() {
        mNotifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel("download", "download", importance)
            channel.description = ticker
            channel.enableLights(true)
            mNotifyManager.createNotificationChannel(channel)
        }
        mBuilder = NotificationCompat.Builder(this, "download")
        mBuilder.setContentTitle(contentTitle)
                //通知首次出现在通知栏，带上升动画效果的
                .setTicker(ticker)
                .setChannelId("download")
                //通常是用来表示一个后台任务
                .setOngoing(true)
                //通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                .setWhen(System.currentTimeMillis())

        //解决5.0系统通知栏白色Icon的问题
        val appIcon = getAppIcon(this)
        var drawableToBitmap: Bitmap? = null
        if (appIcon != null) {
            drawableToBitmap = drawableToBitmap(appIcon)
        }
        if (drawableToBitmap != null) {
            mBuilder.setLargeIcon(drawableToBitmap)
        } else {
            mBuilder.setSmallIcon(applicationInfo.icon)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setSmallIcon(R.drawable.ic_notify_logo)
            mBuilder.color = resources.getColor(R.color.notifyColor)
        } else {
            mBuilder.setSmallIcon(applicationInfo.icon)
        }

    }

    /**
     * 合成更新的Icon
     *
     * @param drawable
     * @return
     */
    fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * 获取App的Icon
     *
     * @param context
     * @return
     */
    private fun getAppIcon(context: Context): Drawable? {
        try {
            return context.packageManager.getApplicationIcon(context.packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        clearNotify()
    }
}