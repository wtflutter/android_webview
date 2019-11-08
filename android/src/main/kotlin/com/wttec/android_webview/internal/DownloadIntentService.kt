package com.wttec.android_webview.internal

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.FileProvider
import android.util.Log
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
    private var id = 0;
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
        fun openMe2(activity: Context, downloadUrl: String, contentTitle: String, id: Int) {
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
        id = intent.getIntExtra("id", 0)
        initNotify()
        startDownload()
    }

    private fun startDownload() {
        var input: InputStream? = null
        var out: FileOutputStream? = null
        try {
            val url = URL(mDownloadUrl)
            val urlConnection = url.openConnection() as HttpURLConnection

            urlConnection.requestMethod = "GET"
            urlConnection.doOutput = false
            urlConnection.connectTimeout = DEFAULT_TIME_OUT
            urlConnection.readTimeout = DEFAULT_TIME_OUT
            urlConnection.setRequestProperty("Connection", "Keep-Alive")
            urlConnection.setRequestProperty("Charset", "UTF-8")
            urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate")

            urlConnection.connect()
            val byteTotal = urlConnection.contentLength
            var bytesum: Long = 0
            input = urlConnection.inputStream
            val apkDownLoadDir = FileUtil.getDownloadPath()
            val apkFile = File(apkDownLoadDir, TMP_NAME)
            if (apkFile.exists()) apkFile.delete()
            out = FileOutputStream(apkFile)
            val buffer = ByteArray(BUFFER_SIZE)

            var oldProgress = 0
            var byteRead: Int = input.read(buffer)

            while (byteRead != -1) {
                bytesum += byteRead.toLong()
                out!!.write(buffer, 0, byteRead)
                val progress = (bytesum * 100L / byteTotal).toInt()
                // 如果进度与之前进度相等，则不更新，如果更新太频繁，否则会造成界面卡顿
                if (progress != oldProgress) {
                    updateProgress(progress)
                }
                oldProgress = progress
                byteRead = input.read(buffer)
            }
            if (id != 0) {
                rename(id)
            } else {
                FileUtil.installAPk(this, rename())
            }
        } catch (e: Exception) {
            Log.e("Exception", "download apk file error:" + e.message)
        } finally {
            if (out != null) {
                try {
                    out.close()
                } catch (ignored: IOException) {

                }

            }
            if (input != null) {
                try {
                    input.close()
                } catch (ignored: IOException) {

                }

            }
        }

    }

    private fun rename(id: Int): File {
        val path = FileUtil.getDownloadPath()
        val old = File(path + TMP_NAME)
        val new = File(path + FileUtil.getFileName(this, id))
        if (new.exists()) new.delete()
        if (old.exists()) old.renameTo(new)
        return new
    }

    private fun rename(): File {
        val path = FileUtil.getDownloadPath()
        val old = File(path + TMP_NAME)
        val new = File(path + APK_FILE_NAME)
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
        if (id != 0) EventBus.getDefault().post(MessageEvent(id, progress))
        mBuilder.setContentText(String.format("%1\$d%%", progress)).setProgress(100, progress, false)
        val pendingintent = PendingIntent.getActivity(this, 0, Intent(), PendingIntent
                .FLAG_UPDATE_CURRENT)
        mBuilder.setContentIntent(pendingintent)
        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build())
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

    private val APK_FILE_NAME = "${System.currentTimeMillis()}.apk"
    private val TMP_NAME = "${System.currentTimeMillis()}-update.tmp"

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
        mNotifyManager.cancel(NOTIFICATION_ID)
    }
}