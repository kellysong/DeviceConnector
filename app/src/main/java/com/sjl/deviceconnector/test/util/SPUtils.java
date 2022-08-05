package com.sjl.deviceconnector.test.util;


import android.content.Context;
import android.content.SharedPreferences;

import com.sjl.deviceconnector.test.app.MyApplication;

/**
 * 偏好参数工具类
 */
public class SPUtils {

    private static final String FILE_NAME = "app_config";
    private static final SharedPreferences sp;

    static {
        sp = MyApplication.getContext().getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
    }

    /**
     * 保存
     *
     * @param key
     * @param object
     */
    public static void put(String key, Object object) {
        SharedPreferences.Editor editor = sp.edit();
        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            editor.putString(key, object.toString());
        }

        editor.commit();
    }

    /**
     * 取值
     * @param key
     * @param defaultObject
     * @return
     */
    public static Object get(String key, Object defaultObject) {
        if (defaultObject instanceof String) {
            return sp.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return sp.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            return sp.getBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Float) {
            return sp.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long) {
            return sp.getLong(key, (Long) defaultObject);
        }
        return null;
    }

    /**
     * 移除key
     * @param key
     */
    public static void remove(String key) {
        sp.edit().remove(key).commit();
    }

}
