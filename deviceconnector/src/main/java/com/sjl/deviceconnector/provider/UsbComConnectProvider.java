package com.sjl.deviceconnector.provider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.sjl.deviceconnector.DeviceContext;
import com.sjl.deviceconnector.ErrorCode;
import com.sjl.deviceconnector.device.usb.UsbHelper;
import com.sjl.deviceconnector.entity.SerialPortConfig;
import com.sjl.deviceconnector.util.LogUtils;

/**
 * Usb Com连接提供者（基于usb-serial-for-android驱动库封装）
 *
 * @author Kelly
 * @version 1.0.0
 * @filename UsbComConnectProvider
 * @time 2020/10/30 11:31
 * @copyright(C) 2020 song
 */
public class UsbComConnectProvider extends BaseConnectProvider {
    private SerialInputOutputManager usbIoManager;
    private UsbSerialPort usbSerialPort;
    private final UsbDevice mUsbDevice;
    private final SerialPortConfig serialPortConfig;

    /**
     * 初始化Usb Com连接提供者
     *
     * @param vendorId 产商id
     * @param productId 产品id
     * @param serialPortConfig 串口配置参数
     */
    public UsbComConnectProvider(int vendorId, int productId, SerialPortConfig serialPortConfig) {
        this(UsbHelper.getDevice(vendorId, productId), serialPortConfig);
    }

    /**
     * 初始化Usb Com连接提供者
     *
     * @param usbDevice
     */
    public UsbComConnectProvider(UsbDevice usbDevice, SerialPortConfig serialPortConfig) {
        this.mUsbDevice = usbDevice;
        this.serialPortConfig = serialPortConfig;
    }


    @SuppressLint("WrongConstant")
    @Override
    public int open() {
        int state = getState();
        if (state == ErrorCode.ERROR_OK) {
            return state;
        }
        if (mUsbDevice == null) {
            LogUtils.e("connection failed: device not found");
            return ErrorCode.ERROR_DEVICE_NOT_FIND;
        }
        UsbManager usbManager = (UsbManager) DeviceContext.getContext().getSystemService(Context.USB_SERVICE);
        UsbSerialDriver usbSerialDriver = UsbSerialProber.getDefaultProber().probeDevice(mUsbDevice);
        if (usbSerialDriver == null) {
            LogUtils.e("connection failed: device driver not found");
            return ErrorCode.ERROR_DEVICE_DRIVER_NOT_FIND;
        }
        UsbDeviceConnection usbConnection = usbManager.openDevice(usbSerialDriver.getDevice());
        if (usbConnection == null && !usbManager.hasPermission(usbSerialDriver.getDevice())) {
            LogUtils.e("connection failed: open failed");
            //触发一次权限申请，正常使用需要在外部申请usb权限
            UsbHelper.getInstance().requestPermission(mUsbDevice);
            return ErrorCode.ERROR_NO_PERMISSION;

        }
        if (!usbManager.hasPermission(usbSerialDriver.getDevice())) {
            LogUtils.e("connection failed: permission denied");
            return ErrorCode.ERROR_PERMISSION_FAIL;
        }
        try {
            usbSerialPort = usbSerialDriver.getPorts().get(0);
            usbSerialPort.open(usbConnection);
            usbSerialPort.setParameters(serialPortConfig.getBaudRate(), serialPortConfig.getDataBits(), serialPortConfig.getStopBits(), serialPortConfig.getParity());
            try {
                usbSerialPort.purgeHwBuffers(true, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            LogUtils.e("connected");
            mConnectState = true;
            return ErrorCode.ERROR_OK;
        } catch (Exception e) {
            LogUtils.e("connection failed", e);
            close();
        }
        return ErrorCode.ERROR_FAIL;
    }


    @Override
    public synchronized int write(byte[] sendParams, int timeout) {
        int state = getState();
        if (state != ErrorCode.ERROR_OK) {
            return state;
        }
        try {
            usbSerialPort.write(sendParams, timeout);
            return ErrorCode.ERROR_OK;
        } catch (Exception e) {
            return ErrorCode.ERROR_FAIL;
        }
    }

    @Override
    public synchronized int read(byte[] sendParams, byte[] buffer, int timeout) {
        int state = getState();
        if (state != ErrorCode.ERROR_OK) {
            return state;
        }
        int ret = write(sendParams, timeout);
        if (ret == ErrorCode.ERROR_OK) {
            try {
                int len = usbSerialPort.read(buffer, timeout);
                return len;
            } catch (Exception e) {
                LogUtils.e("read failed", e);
                return ErrorCode.ERROR_FAIL;
            }
        } else {
            return ret;
        }

    }

    @Override
    public void close() {
        mConnectState = false;
        if (usbIoManager != null) {
            usbIoManager.stop();
            usbIoManager = null;
        }
        close(usbSerialPort);

    }
}
