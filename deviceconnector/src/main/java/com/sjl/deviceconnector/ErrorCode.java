package com.sjl.deviceconnector;

/**
 * 通信传输错误码
 *
 * @author Kelly
 * @version 1.0.0
 * @filename ErrorCode.java
 * @time 2018/4/16 14:29
 * @copyright(C) 2018 song
 */
public class ErrorCode {
    /**
     * 成功
     */
    public static final int ERROR_OK = 0;
    /**
     * 通信超时
     */
    public static final int ERROR_TIMEOUT = -1;
    /**
     * 数据发送错误
     */
    public static final int ERROR_SEND = -2;
    /**
     * 数据接收错误或者丢包
     */
    public static final int ERROR_RECEIVE = -3;
    /**
     * 数据校验错误
     */
    public static final int ERROR_CHECK = -4;
    /**
     * 未连接
     */
    public static final int ERROR_NOT_CONNECTED = -5;
    /**
     * 已连接
     */
    public static final int ERROR_CONNECTED = -6;
    /**
     * 数据为空
     */
    public static final int ERROR_DATA_NULL = -7;

    /**
     * 取消操作
     */
    public static final int ERROR_CANCEL = -8;

    /**
     * 不支持该功能或接口
     */
    public static final int ERROR_NOT_SUPPORTED = -9;
    /**
     * 未初始化
     */
    public static final int ERROR_NOT_INIT = -10;

    /**
     * 设备未找到或不存在
     */
    public static final int ERROR_DEVICE_NOT_FIND = -11;
    /**
     * 设备驱动未找到
     */
    public static final int ERROR_DEVICE_DRIVER_NOT_FIND = -12;
    /**
     * 无权限
     */
    public static final int ERROR_NO_PERMISSION = -14;
    /**
     * 权限拒绝
     */
    public static final int ERROR_PERMISSION_FAIL = -15;
    /**
     * 打开失败
     */
    public static final int ERROR_OPEN_FAIL = -16;


    /**
     * 数据通信失败或失败
     */
    public static final int ERROR_FAIL = -99;


}
