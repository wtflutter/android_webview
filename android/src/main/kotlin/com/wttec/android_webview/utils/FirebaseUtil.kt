package com.wttec.android_webview.utils

import android.app.Activity
import com.aleena.hitunguang.utils.FirebaseSignFragment
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.wttec.android_webview.R

object FirebaseUtil {
    private const val TAG = "FirebaseSignFragment"
    @JvmStatic
    fun login(activity: Activity, callback: (HashMap<String,Any>) -> Unit) {
        val fragment = getFragment(activity)
        fragment.callback = { mobile, uid ->
            val map = HashMap<String, Any>()
            map["id"] = uid
            map["mobile"] = mobile
            callback(map)
        }
        val providers = arrayListOf(
                AuthUI.IdpConfig.PhoneBuilder().build()
        )
        val intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.LoginTheme)
                .setLogo(R.mipmap.ic_launcher)
                .build()
        fragment.startActivityForResult(intent, 99)
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }


    private fun getFragment(activity: Activity): FirebaseSignFragment {
        var fragment = activity.fragmentManager.findFragmentByTag(TAG)
        if (fragment is FirebaseSignFragment) {

        } else {
            fragment = FirebaseSignFragment()
            activity.fragmentManager.apply {
                beginTransaction().add(fragment, TAG).commitAllowingStateLoss()
                executePendingTransactions()
            }
        }
        return fragment
    }
}