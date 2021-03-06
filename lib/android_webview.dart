import 'dart:async';

import 'package:flutter/services.dart';

class AndroidWebview {
  static const MethodChannel _channel = const MethodChannel('com.wttec.android_webview');
  static const EventChannel eventChannel = const EventChannel("com.wttec.android_webview/event");

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

  static void toWeb(String name, int type, int id, String url) {
    _channel.invokeMethod("toWeb", {"url": url, "openType": type, "name": name, "id": id});
  }

  static void toast(String msg) {
    _channel.invokeMethod("toast", msg);
  }

  static Future<String> encrypt(String key, String data) {
    return _channel.invokeMethod("encrypt", {"key": key, "data": data});
  }

  static Future<String> decrypt(String key, String data) {
    return _channel.invokeMethod("decrypt", {"key": key, "data": data});
  }

  static Future<Map<String, dynamic>> get deviceInfo async {
    return getMap("deviceInfo");
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

  static Future<bool> isDownloaded(int id) async {
    final bool result = await _channel.invokeMethod("isDownloaded", id);
    return result;
  }

  static void install(int id) {
    _channel.invokeMethod("install", id);
  }

  static void setHandler(Future<dynamic> handler(MethodCall call)) {
    _channel.setMethodCallHandler(handler);
  }
}
