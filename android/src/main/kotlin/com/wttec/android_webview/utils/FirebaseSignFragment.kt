package com.aleena.hitunguang.utils

import android.app.Activity
import android.app.Fragment
import android.content.Intent
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class FirebaseSignFragment : Fragment() {
    var callback: ((String, String) -> Unit)? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 99) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
                user?.run {
                    callback?.invoke(phoneNumber ?: "", uid)
                }
            } else {
                response?.error?.message?.run {
                }
            }
        }
    }
}