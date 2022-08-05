package com.sjl.deviceconnector.listener;

import android.hardware.usb.UsbDevice;

/**
 * USB设备权限申请回调
 *
 * @author Kelly
 * @version 1.0.0
 * @filename UsbPermissionListener
 * @time 2022/6/7 14:31
 * @copyright(C) 2022 song
 */
public interface UsbPermissionListener {

    void onGranted(UsbDevice usbDevice);

    void onDenied(UsbDevice usbDevice);
}

