package com.sjl.deviceconnector.device.bluetooth.ble;

import java.util.UUID;

/**
 * ble客户端接口
 *
 * @author Kelly
 * @version 1.0.0
 * @filename IBluetoothLeClient
 * @time 2023/3/4 10:00
 * @copyright(C) 2023 song
 */
public interface IBluetoothLeClient {


    /**
     * 读取特征值
     *
     * @param serviceId
     * @param characterId
     * @return
     */
    boolean readCharacteristic(UUID serviceId, UUID characterId);

    /**
     * 写入特征值
     *
     * @param serviceId
     * @param characterId
     * @param value
     * @return
     */
    boolean writeCharacteristic(UUID serviceId, UUID characterId, byte[] value);

    /**
     * 读取描述
     *
     * @param serviceId
     * @param characterId
     * @param descriptorId
     * @return
     */
    boolean readDescriptor(UUID serviceId, UUID characterId, UUID descriptorId);

    /**
     * 写入描述
     *
     * @param serviceId
     * @param characterId
     * @param descriptorId
     * @param value
     * @return
     */
    boolean writeDescriptor(UUID serviceId, UUID characterId, UUID descriptorId, byte[] value);


    /**
     * 设置特征通知
     *
     * @param serviceId
     * @param characterId
     * @param enable
     * @return
     */
    boolean setCharacteristicNotification(UUID serviceId, UUID characterId, boolean enable);

    /**
     * 设置特征知识
     *
     * @param serviceId
     * @param characterId
     * @param enable
     * @return
     */
    boolean setCharacteristicIndication(UUID serviceId, UUID characterId, boolean enable);

    /**
     * 读取Rssi
     *
     * @return
     */
    boolean readRemoteRssi();

    /**
     * 设置mtu
     *
     * @param mtu
     * @return
     */
    boolean requestMtu(int mtu);

}
