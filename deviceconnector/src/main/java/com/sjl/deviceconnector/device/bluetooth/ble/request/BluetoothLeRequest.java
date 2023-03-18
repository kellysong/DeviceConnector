package com.sjl.deviceconnector.device.bluetooth.ble.request;

import java.util.UUID;

/**
 * Ble请求基类
 *
 * @author Kelly
 * @version 1.0.0
 * @filename BluetoothLeRequest
 * @time 2023/3/4 12:15
 * @copyright(C) 2023 song
 */
public class BluetoothLeRequest {


    /**
     * 通讯的服务id
     */
    private UUID service;
    /**
     * 通讯的特征id
     */
    private UUID character;


    public UUID getService() {
        return service;
    }

    public void setService(UUID service) {
        this.service = service;
    }

    public UUID getCharacter() {
        return character;
    }

    public void setCharacter(UUID character) {
        this.character = character;
    }
}
