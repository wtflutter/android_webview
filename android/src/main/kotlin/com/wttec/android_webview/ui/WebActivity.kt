package com.wttec.android_webview.ui

import android.Manifest
import android.annotation.TargetApi
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.webkit.*
import android.widget.Toast
import com.wttec.android_webview.AndroidWebviewPlugin
import com.wttec.android_webview.R
import com.wttec.android_webview.internal.DownloadIntentService
import com.wttec.android_webview.utils.WebViewUtil
import com.wttec.android_webview.utils.checkPermissions
import java.io.File
import java.net.URISyntaxException
import kotlinx.android.synthetic.main.fw_activity_web.*
import kotlinx.android.synthetic.main.fw_web_layout.*
/**
 * Date:       2019/3/20
 * Author:     Su Xing
 * Describe:
 */
class WebActivity : AppCompatActivity() {
    private var uploadMessage: ValueCallback<Uri>? = null
    private var uploadMessageAboveL: ValueCallback<Array<Uri>>? = null
    private var cameraUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fw_activity_web)
        initView()
        initData()
    }

    private fun initView() {
        val drawable = ClipDrawable(ColorDrawable(resources.getColor(R.color.colorPrimary)), Gravity.START, ClipDrawable.HORIZONTAL)
        progressBar!!.progressDrawable = drawable
        iv_finish.setOnClickListener { finish() }
    }

    var webPath = ""

    private fun initData() {
        webPath = intent.getStringExtra("url")

        val webSettings = webView!!.settings
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        webSettings.javaScriptEnabled = true // 设置支持javascript脚本
        webSettings.setSupportZoom(true)
        webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        webSettings.databaseEnabled = true//
        webSettings.domStorageEnabled = true
        webView!!.keepScreenOn = true
        WebViewUtil.addJsBridge(webView)
        webView!!.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView, title: String) {
                super.onReceivedTitle(view, title)
                if (!title.startsWith("http")) {
                    tv_title!!.text = title
                }
            }

            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progressBar!!.progress = newProgress
                super.onProgressChanged(view, newProgress)
            }

            fun openFileChooser(valueCallback: ValueCallback<Uri>, acceptType: String, capture: String) {
                uploadMessage = valueCallback
                openImageUpload()
            }

            override fun onShowFileChooser(webView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: WebChromeClient.FileChooserParams): Boolean {
                uploadMessageAboveL = filePathCallback
                openImageUpload()
                return true
            }

        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                WebViewUtil.initContactScript(webView)
                progressBar.visibility = View.GONE
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                // TODO Auto-generated method stub
                progressBar.visibility = View.VISIBLE
                super.onPageStarted(view, url, favicon)
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.startsWith("intent")) {
                    try {
                        val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        startActivity(intent)
                        webView.goBack()
                    } catch (e: URISyntaxException) {
                        e.printStackTrace()
                    }

                    return true
                } else if (url.startsWith("market://")) {
                    val params = HashMap<String, Any>()
                    params["tag"] = ""
                    params["field1"] = webPath
                    params["field2"] = url
                    val uri = Uri.parse(url)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    val comp = ComponentName("com.android.vending", "com.android.vending.AssetBrowserActivity")
                    intent.component = comp
                    return if (intent.resolveActivity(packageManager) != null) {
                        startActivity(intent)
                        params["field3"] = "success"
                        AndroidWebviewPlugin.channel?.invokeMethod("saveEventGooglePlay", params);
                        finish()
                        true
                    } else {
                        params["field3"] = "failed"
                        AndroidWebviewPlugin.channel?.invokeMethod("saveEventGooglePlay", params);
                        super.shouldOverrideUrlLoading(view, url)
                    }
                } else if (url.startsWith("https://play.google.com/store/apps/details?id=")) {
                    val uri = Uri.parse(url)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.setPackage("com.android.vending")
                    val params = HashMap<String, Any>()
                    params["tag"] = ""
                    params["field1"] = webPath
                    params["field2"] = url
                    return if (intent.resolveActivity(packageManager) != null) {
                        try {
                            startActivity(intent)
                            params["field3"] = "success"
                            AndroidWebviewPlugin.channel?.invokeMethod("saveEventGooglePlay", params)
                            finish()
                            true
                        } catch (e: Exception) {
                            AndroidWebviewPlugin.channel?.invokeMethod("saveEventGooglePlay", params)
                            super.shouldOverrideUrlLoading(view, url)
                        }
                    } else {
                        super.shouldOverrideUrlLoading(view, url)
                    }
                } else {
                    return super.shouldOverrideUrlLoading(view, url)
                }
            }
        }


        webView!!.setDownloadListener { paramAnonymousString1, paramAnonymousString2, paramAnonymousString3, paramAnonymousString4, paramAnonymousLong -> downloadAPK(paramAnonymousString1) }
        webView!!.loadUrl(webPath)
    }

    private fun downloadAPK(downloadUrl: String) {
        val permissionGroup = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        checkPermissions(permissionGroup) {
            if (it) {
                DownloadIntentService.openMe(this, downloadUrl, tv_title.text.toString(), "")
            } else {
                Toast.makeText(applicationContext, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 开始上传
     */
    private fun openImageUpload() {
        val permissionArray = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        checkPermissions(permissionArray) {
            if (it) {
                showChooser()
            } else {
                Toast.makeText(applicationContext, "could not be uploaded", Toast.LENGTH_SHORT).show()
                resetUpload()
            }
        }
    }

    /**
     * 展示选择对话框
     */
    private fun showChooser() {
        val selectPicTypeStr = arrayOf<String>(getString(R.string.tack_camera), getString(R.string.tack_picture))
        AlertDialog.Builder(this)
                .setOnCancelListener(UploadChooserOnCancelListener())
                .setItems(selectPicTypeStr,
                        DialogInterface.OnClickListener { dialog, which ->
                            when (which) {
                                0 -> chooseCameraFile()
                                1 -> chooseImageFile()
                                else -> resetUpload()
                            }
                        }).show()
    }

    /**
     * 选择照相机
     */
    private fun chooseCameraFile() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val imagePath = Environment.getExternalStorageDirectory().path + "/DCIM/Camera/" + (System.currentTimeMillis().toString() + ".jpg")
        val vFile = File(imagePath)
        if (!vFile.exists()) {
            vFile.parentFile.mkdirs()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val pkg = packageName
            cameraUri = FileProvider.getUriForFile(this, "$pkg.fileProvider", vFile)
        } else {
            cameraUri = Uri.fromFile(vFile)
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)
        startActivityForResult(intent, CAMERA_CHOOSER_RESULT_CODE)
    }

    /**
     * 选择本地图片
     */
    private fun chooseImageFile() {
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.addCategory(Intent.CATEGORY_OPENABLE)
        i.type = "image/*"
        startActivityForResult(Intent.createChooser(i, "选择图片"), FILE_CHOOSER_RESULT_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (uploadMessageAboveL != null) {
            onActivityResultAboveL(requestCode, resultCode, data)
        }
        if (uploadMessage == null) return
        var uri: Uri? = null
        if (requestCode == CAMERA_CHOOSER_RESULT_CODE && resultCode == RESULT_OK) {
            uri = cameraUri
        } else if (requestCode == FILE_CHOOSER_RESULT_CODE && resultCode == RESULT_OK) {
            uri = data?.data
        }
        uploadMessage?.onReceiveValue(uri)
        uploadMessage = null
    }


    lateinit var results: Array<Uri>
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun onActivityResultAboveL(requestCode: Int, resultCode: Int, intent: Intent?) {

        if (requestCode == CAMERA_CHOOSER_RESULT_CODE && resultCode == RESULT_OK) {
            results = arrayOf(cameraUri!!)
        } else if (requestCode == FILE_CHOOSER_RESULT_CODE && resultCode == RESULT_OK) {
            if (intent != null) {
                val dataString = intent.dataString
                val clipData = intent.clipData
                if (clipData != null) {
                    results = Array(clipData.itemCount) {
                        val item = clipData.getItemAt(it)
                        item.uri
                    }
                }
                if (dataString != null)
                    results = arrayOf(Uri.parse(dataString))
            }
        }
        if (::results.isInitialized) {
            uploadMessageAboveL?.onReceiveValue(results)
        }
        uploadMessageAboveL = null
    }

    /**
     * 上传图片选择器取消监听
     */
    private inner class UploadChooserOnCancelListener : DialogInterface.OnCancelListener {
        override fun onCancel(dialogInterface: DialogInterface) {
            resetUpload()
        }
    }

    /**
     * 重置上传
     */
    private fun resetUpload() {
        if (uploadMessage != null) {
            uploadMessage!!.onReceiveValue(null)
            uploadMessage = null
        }
        if (uploadMessageAboveL != null) {
            uploadMessageAboveL!!.onReceiveValue(null)
            uploadMessageAboveL = null
        }
    }


    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putString("url", webView!!.url)
    }

    override fun onBackPressed() {
        if (webView!!.canGoBack()) {
            webView!!.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        webView.removeAllViews()
        webView.destroy()
        super.onDestroy()
    }

    companion object {
        private val CAMERA_CHOOSER_RESULT_CODE = 9999
        private val FILE_CHOOSER_RESULT_CODE = 10000

        fun openMe(context: Context, webLoadUrl: String) {
            val intent = Intent(context, WebActivity::class.java)
            intent.putExtra("url", webLoadUrl)
            context.startActivity(intent)
        }
    }


}
