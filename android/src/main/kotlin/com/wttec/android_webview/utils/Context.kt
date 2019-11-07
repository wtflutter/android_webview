package com.wttec.android_webview.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.iid.FirebaseInstanceId
import com.yanzhenjie.permission.AndPermission
import kotlin.reflect.KClass

fun Context.versionName(): String {
    val pi = packageManager.getPackageInfo(packageName, 0)
    return pi.versionName
}

@JvmOverloads
fun Context.toActivity(clazz: Class<out Activity>, bundle: Bundle? = null) {
    val intent = Intent(this, clazz)
    bundle?.let {
        intent.putExtras(it)
    }
    if (this is Activity) {
        startActivityForResult(intent, 1)
    } else {
        startActivity(intent)
    }
}

@JvmOverloads
fun Context.toActivity(clazz: KClass<out Activity>, bundle: Bundle? = null) {
    val intent = Intent(this, clazz.java)
    bundle?.let {
        intent.putExtras(it)
    }
    if (this is Activity) {
        startActivityForResult(intent, 1)
    } else {
        startActivity(intent)
    }
}

/**
 * 是否含有权限
 */
fun Context.hasPermission(permission: String): Boolean {
    return checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}

fun Context.hasPermissions(permissions: Array<String>): Boolean {
    permissions.forEach {
        if (!hasPermission(it)) return false
    }
    return true
}

interface PermissionCallback {
    fun onGranted(granted: Boolean)
}

/**
 * 动态申请权限 java: ContextKt.checkPermission()
 */
fun Context.checkPermission(permission: String, callback: PermissionCallback) {
    checkPermission(permission) {
        callback.onGranted(it)
    }
}

/**
 * 动态申请权限 kotlin（checkPermission(permission){}）
 */
fun Context.checkPermission(permission: String, callback: (Boolean) -> Unit) {
    if (hasPermission(permission)) {
        callback(true)
        return
    }
    AndPermission.with(this)
        .runtime()
        .permission(permission)
        .onGranted {
            callback(true)
        }.onDenied {
            callback(false)
        }.start()
}

/**
 * 动态申请权限
 */
fun Context.checkPermissions(permissions: Array<String>, callback: PermissionCallback) {
    checkPermissions(permissions) {
        callback.onGranted(it)
    }
}

fun Context.checkPermissions(permissions: Array<String>, callback: (Boolean) -> Unit) {
    if (hasPermissions(permissions)) {
        callback(true)
        return
    }
    AndPermission.with(this)
        .runtime()
        .permission(permissions)
        .onGranted {
            callback(true)
        }.onDenied {
            callback(false)
        }.start()
}

fun Context.getToken(callback:(Map<String,Any>)->Unit) {
    if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) != 0) return
    FirebaseInstanceId.getInstance()
            .instanceId
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    it.result?.token?.apply {
                        callback(deviceParams(this))
                    }
                }
            }
}
