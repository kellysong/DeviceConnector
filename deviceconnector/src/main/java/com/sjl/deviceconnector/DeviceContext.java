package com.sjl.deviceconnector;

import android.content.Context;

import com.sjl.deviceconnector.util.LogUtils;

/**
 * 设备上下文
 *
 * @author Kelly
 */
public class DeviceContext {
    private static Context context;

    public static void init(Context context,boolean debug) {
        DeviceContext.context = context;
        LogUtils.init(debug);
    }

    public static Context getContext() {
        if (context == null) {
            throw new RuntimeException("DeviceContext未初始化");
        }
        return context;
    }
}
