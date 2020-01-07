package com.wttec.android_webview.utils

import android.app.Fragment
import android.content.Intent
import android.util.Log

class InstallFragment : Fragment() {
    var callback: ((Boolean) -> Unit)? = null
    var pkgName = ""
    var lastTime = System.currentTimeMillis()
    override fun onResume() {
        super.onResume()
        val t = System.currentTimeMillis()
        if (t - lastTime > 200) {
            val install = FileUtil.isInstall(activity, pkgName)
            callback?.invoke(install)
            callback = null
        }
        lastTime = t
        Log.e("install", "onResume")
    }

    override fun onStop() {
        super.onStop()
        Log.e("install", "onStop")
    }

    override fun onPause() {
        super.onPause()
        Log.e("install", "onPause")
    }
}