package com.sjl.deviceconnector.device.bluetooth.ble.request;

/**
 * 特征写请求
 *
 * @author Kelly
 * @version 1.0.0
 * @filename CharacteristicWriteRequest
 * @time 2023/3/4 12:41
 * @copyright(C) 2023 song
 */
public class CharacteristicWriteRequest extends BluetoothLeRequest {


    /**
     * 待发送的数据
     */
    private byte[] bytes;

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
