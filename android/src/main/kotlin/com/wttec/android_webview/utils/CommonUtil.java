package com.wttec.android_webview.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import java.io.File;

/**
 * Date:       2019/3/21
 * Author:     Su Xing
 * Describe:
 */
public class CommonUtil {

    /**
     * 获取网络状态
     * 1    WIFI
     * 2    2G
     * 3    3G
     * 4    4G
     *
     * @param context
     *
     * @return
     */
    public static int getNetType(Context context) {
        //结果返回值
        int netType = 4;
        //获取手机所有连接管理对象
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //获取NetworkInfo对象
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        //NetworkInfo对象为空 则代表没有网络
        if (networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_WIFI) {
            //WIFI
            netType = 1;
        } else if (nType == ConnectivityManager.TYPE_MOBILE) {
            int nSubType = networkInfo.getSubtype();
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (nSubType == TelephonyManager.NETWORK_TYPE_LTE
                    && telephonyManager != null
                    && !telephonyManager.isNetworkRoaming()) {
                netType = 4;
                //3G   联通的3G为UMTS或HSDPA 电信的3G为EVDO
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_UMTS
                    || nSubType == TelephonyManager.NETWORK_TYPE_HSDPA
                    || nSubType == TelephonyManager.NETWORK_TYPE_TD_SCDMA
                    || nSubType == TelephonyManager.NETWORK_TYPE_EVDO_0
                    && telephonyManager != null
                    && !telephonyManager.isNetworkRoaming()) {
                netType = 3;
                //2G 移动和联通的2G为GPRS或EGDE，电信的2G为CDMA
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_GPRS
                    || nSubType == TelephonyManager.NETWORK_TYPE_EDGE
                    || nSubType == TelephonyManager.NETWORK_TYPE_CDMA
                    && telephonyManager != null
                    && !telephonyManager.isNetworkRoaming()) {
                netType = 2;
            } else {
                netType = 4;
            }
        }
        return netType;
    }


    public static String getWifiName(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager == null) {
                return "";
            } else {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                return wifiInfo.getSSID().replaceAll("\"", "");
            }

        } catch (Exception e) {
            return "";
        }
    }
    /**
     * 检测是否wifi
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // 获取NetworkInfo对象
        if (manager != null) {
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            //判断NetworkInfo对象是否为空 并且类型是否为WIFI
            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return networkInfo.isAvailable();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


    /**
     * 获取运营商
     * 1-移动；2-联通；3-电信；4-其他 5 no-sim卡
     */
    public static int getSimOperator(Context context) {
        int simOperatorType;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
//            String simOperator = telephonyManager.getSimOperator();
            String simOperatorName = telephonyManager.getSimOperatorName();
            if ("中国移动".equals(simOperatorName)) {
                simOperatorType = 1;
            } else if ("中国联通".equals(simOperatorName)) {
                simOperatorType = 2;
            } else if ("中国电信".equals(simOperatorName)) {
                simOperatorType = 3;
            } else {
                simOperatorType = 4;
            }
        } else {
            simOperatorType = 5;
        }
        return simOperatorType;
    }


    /**
     * Return whether device is rooted.
     *
     * 1 == true
     * 2 == false
     */
    public static int getDeviceRootedState() {
        String su = "su";
        String[] locations = {"/system/bin/", "/system/xbin/", "/sbin/", "/system/sd/xbin/",
                "/system/bin/failsafe/", "/data/local/xbin/", "/data/local/bin/", "/data/local/"};
        for (String location : locations) {
            if (new File(location + su).exists()) {
                return 1;
            }
        }
        return 2;
    }

}
