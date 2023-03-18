package com.sjl.deviceconnector.device.bluetooth.ble.request;

import java.util.UUID;

/**
 * 描述符读请求
 *
 * @author Kelly
 * @version 1.0.0
 * @filename DescriptorReadRequest
 * @time 2023/3/4 14:19
 * @copyright(C) 2023 song
 */
public class DescriptorReadRequest extends BluetoothLeRequest {

    /**
     * 通讯的描述符id
     */
    private UUID descriptor;


    public UUID getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(UUID descriptor) {
        this.descriptor = descriptor;
    }
}
