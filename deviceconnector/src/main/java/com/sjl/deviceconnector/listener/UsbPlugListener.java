package com.sjl.deviceconnector.listener;

import android.hardware.usb.UsbDevice;

/**
 * USB设备插拔回调
 *
 * @author Kelly
 * @version 1.0.0
 * @filename UsbPlugListener
 * @time 2022/6/9 11:51
 * @copyright(C) 2022 song
 */
public interface UsbPlugListener {

    void onAttached(UsbDevice usbDevice);

    void onDetached(UsbDevice usbDevice);
}
