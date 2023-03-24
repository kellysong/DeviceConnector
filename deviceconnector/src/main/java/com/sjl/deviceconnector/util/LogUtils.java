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
    private static boolean debug = false;
    private LogUtils() {

    }
    public static void init(boolean debug){
        LogUtils.debug = debug;
    }
    private static final String TAG = "DEVICE_CONNECTOR";

    public static void i(String str) {
        Log.i(TAG, str);
    }

    public static void w(String str) {
        Log.w(TAG, str);
    }

    public static void e(String str) {
        Log.e(TAG, str);
    }

    public static void e(String str, Exception e) {
        Log.e(TAG, str, e);
    }

    public static boolean isDebug() {
        return debug;
    }
}
