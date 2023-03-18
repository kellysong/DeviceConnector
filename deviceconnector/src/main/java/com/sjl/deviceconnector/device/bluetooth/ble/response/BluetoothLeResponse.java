package com.sjl.deviceconnector.device.bluetooth.ble.response;

import java.util.Arrays;
import java.util.UUID;

/**
 * Ble响应
 *
 * @author Kelly
 * @version 1.0.0
 * @filename BluetoothLeResponse
 * @time 2023/3/4 15:18
 * @copyright(C) 2023 song
 */
public class BluetoothLeResponse {
    private int code;
    private byte[] data;
    private int rssi = -1;
    private int mtu = -1;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getMtu() {
        return mtu;
    }

    public void setMtu(int mtu) {
        this.mtu = mtu;
    }

    public void copy(BluetoothLeResponse buffer) {
        this.code = buffer.code;
        this.data = buffer.data;
        this.rssi = buffer.rssi;
        this.mtu = buffer.mtu;

    }

    public void reset() {
        this.code = 0;
        this.data = null;
        this.rssi = -1;
        this.mtu = -1;
    }

    @Override
    public String toString() {
        return "{" +
                "code=" + code +
                ", data=" + Arrays.toString(data) +
                ", rssi=" + rssi +
                ", mtu=" + mtu +
                '}';
    }
}
