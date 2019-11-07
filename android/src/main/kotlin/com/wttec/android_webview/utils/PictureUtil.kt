package com.wttec.android_webview.utils

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.provider.MediaStore


object PictureUtil {
    fun openPicture(activity: Activity, callback: (String?) -> Unit) {
        activity.checkPermissions(arrayOf(Manifest.permission_group.STORAGE)) {
            if (it) {
                val f = getFragment(activity)
                f.callback = callback
                val intentToPickPic = Intent(Intent.ACTION_PICK, null)
                intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
                f.startActivityForResult(intentToPickPic, 200)
            }
        }
    }

    private fun getFragment(activity: Activity): PictureFragment {
        var fragment = activity.fragmentManager.findFragmentByTag("picture")
        if (fragment is PictureFragment) {

        } else {
            fragment = PictureFragment()
            activity.fragmentManager.apply {
                beginTransaction().add(fragment, "picture").commitAllowingStateLoss()
                executePendingTransactions()
            }
        }
        return fragment
    }
}