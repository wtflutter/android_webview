package com.wttec.android_webview.internal;

public class MessageEvent {
    public String id;
    public int progress;

    public MessageEvent(String id, int progress) {
        this.id = id;
        this.progress = progress;
    }
}
