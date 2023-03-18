package com.sjl.deviceconnector.device.bluetooth.ble;

import java.util.UUID;

/**
 * TODO
 *
 * @author Kelly
 * @version 1.0.0
 * @filename BluetoothLeNotifyListener
 * @time 2023/3/4 17:59
 * @copyright(C) 2023 song
 */
public interface BluetoothLeNotifyListener {
    void onNotify(UUID serviceId, UUID characterId, byte[] value);
}
