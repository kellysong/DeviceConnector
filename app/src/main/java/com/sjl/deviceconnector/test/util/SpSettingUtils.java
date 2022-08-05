package com.sjl.deviceconnector.test.util;


import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.sjl.deviceconnector.entity.SerialPortConfig;
import com.sjl.deviceconnector.test.app.AppConstant;
import com.sjl.deviceconnector.test.app.MyApplication;
import com.sjl.deviceconnector.test.entity.SerialPortInfo;
import com.sjl.deviceconnector.test.entity.WifiInfo;


/**
 * TODO
 *
 * @author Kelly
 * @version 1.0.0
 * @filename SpSettingUtils
 * @time 2022/7/2 19:16
 * @copyright(C) 2022 song
 */
public class SpSettingUtils {

    private static Gson gson = new Gson();

    private SpSettingUtils() {
    }


    public static void saveSerialPortInfo(SerialPortInfo serialPortInfo) {
        String s = gson.toJson(serialPortInfo);
        SPUtils.put(AppConstant.SpParams.SERIAL_PORT_INFO, s);
    }

    public static SerialPortInfo getSerialPortInfo() {
        SerialPortInfo serialPortInfo;
        String  serialPortConfigStr = (String) SPUtils.get(AppConstant.SpParams.SERIAL_PORT_INFO, "");
        if (!TextUtils.isEmpty(serialPortConfigStr)){
            serialPortInfo = new Gson().fromJson(serialPortConfigStr, SerialPortInfo.class);
        }else {
            serialPortInfo = new SerialPortInfo();
        }
        return serialPortInfo;
    }


    public static void saveSysWifiInfo(WifiInfo wifiInfo) {
        String s = gson.toJson(wifiInfo);
        SPUtils.put(AppConstant.SpParams.WIFI_INFO, s);
    }

    public static WifiInfo getWifiInfo() {
        WifiInfo wifiInfo;
        String  s = (String) SPUtils.get(AppConstant.SpParams.WIFI_INFO, "");
        if (!TextUtils.isEmpty(s)){
            wifiInfo = new Gson().fromJson(s, WifiInfo.class);
        }else {
            wifiInfo = new WifiInfo();
        }
        return wifiInfo;
    }

}
