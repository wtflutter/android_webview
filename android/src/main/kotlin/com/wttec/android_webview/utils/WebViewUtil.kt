package com.wttec.android_webview.utils

import android.webkit.WebView
import java.lang.ref.WeakReference

object WebViewUtil {
    private const val jsObjName = "wtJsBridge"

    @JvmStatic
    fun addJsBridge(webView: WebView) {
        webView.addJavascriptInterface(JsObject(WeakReference(webView)), jsObjName)
    }

    @JvmStatic
    fun initContactScript(webView: WebView) {
        val script = """
            javascript:(function(){
            window.$jsObjName.contactCallback = function(args){
                window.$jsObjName.call(args);
            };
            window.$jsObjName.getContacts = function(callback){
                window.$jsObjName.call = callback;
                console.log("getContact...");
                window.$jsObjName._getContact();
            };
            })();
        """
        webView.loadUrl(script)
    }
}