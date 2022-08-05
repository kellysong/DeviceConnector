package com.sjl.deviceconnector.listener;

/**
 * 广播观察者
 *
 * @author Kelly
 * @version 1.0.0
 * @filename ReceiverObservable
 * @time 2022/8/4 16:19
 * @copyright(C) 2022 song
 */
public interface ReceiverObservable {
    /**
     * 注册广播
     */
    void registerReceiver();

    /**
     * 反注册广播
     */
    void unregisterReceiver();
}
