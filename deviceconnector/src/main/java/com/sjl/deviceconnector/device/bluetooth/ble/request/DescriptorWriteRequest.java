package com.sjl.deviceconnector.device.bluetooth.ble.request;

import java.util.UUID;

/**
 * 描述符写请求
 *
 * @author Kelly
 * @version 1.0.0
 * @filename DescriptorWriteRequest
 * @time 2023/3/4 12:41
 * @copyright(C) 2023 song
 */
public class DescriptorWriteRequest extends BluetoothLeRequest {
    /**
     * 通讯的描述符id
     */
    private UUID descriptor;
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

    public UUID getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(UUID descriptor) {
        this.descriptor = descriptor;
    }
}
