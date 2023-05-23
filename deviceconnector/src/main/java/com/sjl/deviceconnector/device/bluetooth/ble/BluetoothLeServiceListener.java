package com.sjl.deviceconnector.device.bluetooth.ble;

import android.bluetooth.BluetoothGattService;

import java.util.List;

/**
 * 服务发现回调
 *
 * @author Kelly
 * @version 1.0.0
 * @filename BluetoothLeServiceListener
 * @time 2023/5/17 14:28
 * @copyright(C) 2023 song
 */
public interface BluetoothLeServiceListener {
    /**
     * 服务发现回调
     * @param status 0成功时，bluetoothGattServices有值
     * @param bluetoothGattServices
     */
    void onServicesDiscovered(int status, List<BluetoothGattService> bluetoothGattServices);
}
