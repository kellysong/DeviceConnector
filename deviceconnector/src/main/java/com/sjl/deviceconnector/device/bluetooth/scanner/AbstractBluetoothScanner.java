package com.sjl.deviceconnector.device.bluetooth.scanner;

import com.sjl.deviceconnector.entity.BluetoothScanResult;
import com.sjl.deviceconnector.listener.BluetoothScanListener;

/**
 * TODO
 *
 * @author Kelly
 * @version 1.0.0
 * @filename AbstractBluetoothScanner
 * @time 2023/3/3 14:58
 * @copyright(C) 2023 song
 */
public abstract class AbstractBluetoothScanner implements BluetoothScanner{

    protected BluetoothScanListener bluetoothScanListener;
    protected String address;



    /**
     * 扫描监听
     * @param bluetoothScanListener
     */
    public void setBluetoothScanListener(BluetoothScanListener bluetoothScanListener) {
        this.bluetoothScanListener = bluetoothScanListener;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    public void removeListener(){
        this.address = null;
        this.bluetoothScanListener = null;
    }

    protected void notifyDeviceFounded(BluetoothScanResult device) {
        if (bluetoothScanListener != null) {
            bluetoothScanListener.onDeviceFound(device);
        }
    }

    public void notifyScanFinish() {
        if (bluetoothScanListener != null) {
            bluetoothScanListener.onScanFinish();
        }
    }

}
