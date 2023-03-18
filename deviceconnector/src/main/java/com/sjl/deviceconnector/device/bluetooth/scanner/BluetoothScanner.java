package com.sjl.deviceconnector.device.bluetooth.scanner;

import com.sjl.deviceconnector.listener.BluetoothScanListener;

/**
 * TODO
 *
 * @author Kelly
 * @version 1.0.0
 * @filename BluetoothScanner
 * @time 2023/3/3 14:33
 * @copyright(C) 2023 song
 */
public interface BluetoothScanner {

    /**
     * 开始扫描
     */
    void startScan();

    /**
     * 停止扫描
     */
    void stopScan();
}
