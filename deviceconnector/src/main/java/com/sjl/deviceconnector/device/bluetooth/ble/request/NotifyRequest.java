package com.sjl.deviceconnector.device.bluetooth.ble.request;

/**
 * 特性值通知请求（单向）
 *
 * @author Kelly
 * @version 1.0.0
 * @filename NotifyRequest
 * @time 2023/3/4 14:33
 * @copyright(C) 2023 song
 */
public class NotifyRequest extends BluetoothLeRequest {
    /**
     * true开启通知，false关闭
     */
    private boolean enable;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
