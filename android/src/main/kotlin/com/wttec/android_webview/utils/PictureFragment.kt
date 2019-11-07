package com.wttec.android_webview.utils

import android.app.Fragment
import android.content.Intent

class PictureFragment : Fragment() {
    var callback: ((String?) -> Unit)? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200) {
            if (data != null) {
                callback?.invoke(FileUtil.getFilePathByUri(activity, data!!.data))
            }
        }
    }
}