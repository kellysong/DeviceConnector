package com.sjl.deviceconnector.test.util;

import java.lang.reflect.ParameterizedType;

/**
 * TODO
 *
 * @author Kelly
 * @version 1.0.0
 * @filename TUtils
 * @time 2022/6/7 11:47
 * @copyright(C) 2022 song
 */
public class TUtils {

    public static Class getClass(Class clz) {
        ParameterizedType type = (ParameterizedType) clz.getGenericSuperclass();
        Class cls = (Class) type.getActualTypeArguments()[0];
        return cls;
    }
}
