package com.sjl.deviceconnector.device.bluetooth.ble.request;

/**
 * 特性值指示请求
 * <p> indicate：译为“指示”，它是服务器给客户端发送数据的方式。它在使用上比notify多一个应答的步骤。</p>
 *
 * @author Kelly
 * @version 1.0.0
 * @filename IndicateRequest
 * @time 2023/3/4 14:32
 * @copyright(C) 2023 song
 */
public class IndicateRequest extends BluetoothLeRequest {
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
