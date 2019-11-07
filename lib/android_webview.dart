import 'dart:async';

import 'package:flutter/services.dart';

class AndroidWebview {
  static const MethodChannel _channel = const MethodChannel('com.wttec.android_webview');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<Map<String, dynamic>> get loginMap async {
    return getMap("login");
  }

  static void logout() async {
    return _channel.invokeMethod("logout");
  }

  static void toWeb(String url, {int type}) {
    _channel.invokeMethod("toWeb", {"url": url, "openType": type});
  }

  static Future<Map<String, dynamic>> get tokenMap async {
    return getMap("token");
  }

  static Future<Map<String, dynamic>> getMap(String key) async {
    final Map<dynamic, dynamic> result = await _channel.invokeMethod(key);
    Map<String, dynamic> map = {};
    result.forEach((k, v) {
      map["$k"] = v;
    });
    return map;
  }

  static Future<String> get picture async {
    final String result = await _channel.invokeMethod("openPicture");
    return result;
  }

  static Future<String> get version async {
    final String result = await _channel.invokeMethod("version");
    return result;
  }

  static void setHandler(Future<dynamic> handler(MethodCall call)) {
    _channel.setMethodCallHandler(handler);
  }
}
