package com.sjl.deviceconnector.util;

import android.util.Log;

/**
 * TODO
 *
 * @author Kelly
 * @version 1.0.0
 * @filename LogUtils
 * @time 2020/8/29 18:56
 * @copyright(C) 2020 song
 */
public class LogUtils {
    private static final String TAG = "DEVICE_CONNECTOR";

    private static boolean debug = false;

    private LogUtils() {

    }
    public static void init(boolean debug){
        LogUtils.debug = debug;
    }

    public static void i(String str) {
        if (!debug){
            return;
        }
        Log.i(TAG, str);
    }

    public static void w(String str) {
        if (!debug){
            return;
        }
        Log.w(TAG, str);
    }

    public static void e(String str) {
        if (!debug){
            return;
        }
        Log.e(TAG, str);
    }

    public static void e(String str, Exception e) {
        if (!debug){
            return;
        }
        Log.e(TAG, str, e);
    }

    public static boolean isDebug() {
        return debug;
    }
}
