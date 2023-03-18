package com.sjl.deviceconnector.test.entity;

/**
 * 连接方式枚举
 *
 * @author Kelly
 * @version 1.0.0
 * @filename ConnectWay
 * @time 2022/7/25 11:50
 * @copyright(C) 2022 song
 */
public enum ConnectWay {
    SERIAL_PORT("串口"),
    USB_COM("Usb Com"),
    USB("Usb"),
    BLUETOOTH("Bluetooth Classic"),
    WIFI("Wifi"),
    BLUETOOTH_Low_Energy("Bluetooth Low Energy"),
    ;

    private final String name;

    ConnectWay(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
