package com.sjl.deviceconnector;

import android.content.Context;
import android.os.Handler;

import com.sjl.deviceconnector.util.LogUtils;

/**
 * 设备上下文
 *
 * @author Kelly
 */
public class DeviceContext {
    private static Context context;

    private static Handler mMainHandler;

    public static void init(Context context,boolean debug) {
        DeviceContext.context = context;
        LogUtils.init(debug);
    }

    public static Context getContext() {
        checkContext();
        return context;
    }

    private static void checkContext() {
        if (context == null) {
            throw new RuntimeException("DeviceContext未初始化");
        }
    }

    public static Handler mainHandler() {
        checkContext();
        if (mMainHandler == null){
            mMainHandler = new Handler(context.getMainLooper());
        }
        return mMainHandler;
    }
}
