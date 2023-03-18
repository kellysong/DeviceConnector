package com.sjl.deviceconnector.device.bluetooth.ble.request;

/**
 * mtu设置请求
 *
 * @author Kelly
 * @version 1.0.0
 * @filename MtuRequest
 * @time 2023/3/4 14:33
 * @copyright(C) 2023 song
 */
public class MtuRequest extends BluetoothLeRequest {
    private int mtu;

    public int getMtu() {
        return mtu;
    }

    public void setMtu(int mtu) {
        this.mtu = mtu;
    }
}
