package com.sjl.deviceconnector.listener;


/**
 * 设备连接状态监听
 *
 * @author Kelly
 * @version 1.0.0
 * @filename ConnectedListener
 * @time 2022/8/4 16:14
 * @copyright(C) 2022 song
 */
public interface ConnectedListener<T> {

    void onResult(T device,boolean connected);
}
