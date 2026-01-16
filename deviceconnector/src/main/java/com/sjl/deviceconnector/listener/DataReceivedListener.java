package com.sjl.deviceconnector.listener;

/**
 * 数据接收监听器
 *
 * @author Kelly
 * @version 1.0.0
 * @filename DataReceivedListener
 * @time 2026/1/16 9:18
 * @copyright(C) 2026 song
 */
public interface DataReceivedListener {
    void onDataReceived(byte[] data);
}
