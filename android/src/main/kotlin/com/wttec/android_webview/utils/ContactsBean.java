package com.wttec.android_webview.utils;


import android.support.annotation.Keep;

import java.util.List;

/**
 * Date:       2019-06-26
 * Author:     Su Xing
 * Describe:
 */
@Keep
public class ContactsBean {
    public long id;
    public String name;
    public int contactTimes;
    public long lastContactTime;
    public List<String> numList;
}
