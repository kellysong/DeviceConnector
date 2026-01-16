package com.sjl.deviceconnector.util;

/**
 * TODO
 *
 * @author Kelly
 * @version 1.0.0
 * @filename ByteUtils
 * @time 2020/8/29 19:32
 * @copyright(C) 2020 song
 */
public class ByteUtils {
    /**
     * 16进制字符串转字节数组
     *
     * @param hex
     * @return
     */
    public static byte[] hexStringToByteArr(String hex) {
        int l = hex.length() / 2;
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            ret[i] = (byte) Integer.valueOf(hex.substring(i * 2, i * 2 + 2), 16).byteValue();
        }
        return ret;
    }

    /**
     * 字节数组转16进制字符串
     * @param b
     * @return
     */
    public static String byteArrToHexString(byte[] b) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            result.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString().toUpperCase();
    }


    /**
     * 将源数组追加到目标数组，合并两个byte数组
     *
     * @param data1 原数组1
     * @param data2 原数组2
     * @param size   长度
     * @return 返回一个新的数组，包括了原数组1和原数组2
     */
    public static byte[] arrayAppend(byte[] data1, byte[] data2, int size) {
        if (data1 == null && data2 == null) {
            return null;
        } else if (data1 == null) {
            byte[] data = new byte[size];
            System.arraycopy(data2, 0, data, 0, size);
            return data;
            //return data2;
        } else if (data2 == null) {
            byte[] data = new byte[data1.length];
            System.arraycopy(data1, 0, data, 0, data1.length);
            return data;
            //return data1;
        } else {
            //合并数组
            byte[] data = new byte[data1.length + size];
            System.arraycopy(data1, 0, data, 0, data1.length);
            System.arraycopy(data2, 0, data, data1.length, size);
            return data;
        }

    }
}
