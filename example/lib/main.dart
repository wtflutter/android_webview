import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:android_webview/android_webview.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  static const EventChannel eventChannel = const EventChannel("com.wttec.android_webview/event");

  @override
  void initState() {
    super.initState();
    initPlatformState();
    initCallback();
    eventChannel.receiveBroadcastStream().listen((obj) {
      print("event:$obj");
    });
  }

  void initCallback() {
    AndroidWebview.setHandler((MethodCall method) async {
      print("$method");
      return "";
    });
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await AndroidWebview.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: <Widget>[
            Padding(
              padding: EdgeInsets.all(30),
              child: GestureDetector(
                onTap: () async {
                  var mobile = await AndroidWebview.loginMap;
                  print("mobile:$mobile");
                },
                child: Container(
                  height: 44,
                  width: 100,
                  child: Center(
                    child: Text("login"),
                  ),
                ),
              ),
            ),
            Padding(
              padding: EdgeInsets.all(30),
              child: GestureDetector(
                onTap: () async {
                  var token = await AndroidWebview.tokenMap;
                  print("token:$token");
                },
                child: Container(
                  height: 44,
                  width: 100,
                  color: Colors.lightBlue,
                  child: Center(
                    child: Text("toke"),
                  ),
                ),
              ),
            ),
            Padding(
              padding: EdgeInsets.all(30),
              child: GestureDetector(
                onTap: () async {
                  await AndroidWebview.toWeb("豌豆荚", 4, 1, "https://ucan.25pp.com/Wandoujia_web_seo_baidu_homepage.apk");
                },
                child: Container(
                  height: 44,
                  width: 100,
                  color: Colors.lightBlue,
                  child: Center(
                    child: Text("下载"),
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
