package com.sjl.deviceconnector.provider;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.sjl.deviceconnector.ErrorCode;
import com.sjl.deviceconnector.util.LogUtils;

import java.util.UUID;

/**
 * 蓝牙连接提供者（经典蓝牙）
 * <p>连接流程：打开蓝牙开关->搜索蓝牙->配对蓝牙->连接蓝牙->读写数据->断开蓝牙->关闭蓝牙开关</p>
 * @author Kelly
 * @version 1.0.0
 * @filename BluetoothConnectProvider.java
 * @time 2018/4/13 8:43
 * @copyright(C) 2018 song
 */
public class BluetoothConnectProvider extends BaseIoConnectProvider {
    /**
     * SPP服务UUID号
     */
    public final static String BLUETOOTH_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private final BluetoothDevice mBluetoothDevice;
    private BluetoothSocket mBluetoothSocket;
    private String uuid = BLUETOOTH_UUID;


    /**
     * 初始化一个蓝牙提供者
     *
     * @param address 蓝牙mac地址
     */
    public BluetoothConnectProvider(String address) {
        this(address, BLUETOOTH_UUID);
    }

    /**
     * 初始化一个蓝牙提供者
     *
     * @param bluetoothDevice 蓝牙设备
     */
    public BluetoothConnectProvider(BluetoothDevice bluetoothDevice) {
        this(bluetoothDevice, BLUETOOTH_UUID);
    }

    /**
     * 根据指定蓝牙服务UUID,初始化一个蓝牙提供者
     *
     * @param address
     * @param uuid
     */
    public BluetoothConnectProvider(String address, String uuid) {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        if (defaultAdapter == null) {
            throw new NullPointerException("设备不支持蓝牙");
        }
        this.mBluetoothDevice = defaultAdapter.getRemoteDevice(address);
        this.uuid = uuid;
    }

    /**
     * 根据指定蓝牙服务UUID,初始化一个蓝牙提供者
     *
     * @param bluetoothDevice
     * @param uuid
     */
    public BluetoothConnectProvider(BluetoothDevice bluetoothDevice, String uuid) {
        this.mBluetoothDevice = bluetoothDevice;
        this.uuid = uuid;
    }



    @Override
    public int open() {
        int state = getState();
        if (state == ErrorCode.ERROR_OK) {
            return state;
        }
        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(uuid));
            mBluetoothSocket.connect();
            mConnectState = true;
            mInputStream = mBluetoothSocket.getInputStream();
            mOutputStream = mBluetoothSocket.getOutputStream();
            return ErrorCode.ERROR_OK;
        } catch (Exception e) {
            LogUtils.e("蓝牙连接异常", e);
            close();
            return ErrorCode.ERROR_FAIL;
        }
    }

    @Override
    public void close() {
        super.close();
        mConnectState = false;
        close(getOutputStream());
        close(getInputStream());
        close(mBluetoothSocket);
    }
}
