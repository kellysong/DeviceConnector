package com.sjl.deviceconnector.entity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.List;

/**
 * TODO
 *
 * @author Kelly
 * @version 1.0.0
 * @filename BluetoothScanResult
 * @time 2023/3/3 14:52
 * @copyright(C) 2023 song
 */
public class BluetoothScanResult {

    private BluetoothDevice device;

    /**
     * 可理解成设备的信号值。该数值是一个负数，越大则信号越强。
     */
    private int rssi;

    /**
     * 远程设备提供的广播数据的内容。
     */
    private byte[] scanRecordBytes;
    /**
     * 远程设备提供的广播数据的内容。
     */
    private ScanRecord scanRecord;



    public BluetoothScanResult(BluetoothDevice device, int rssi, byte[] scanRecordBytes) {
        this.device = device;
        this.rssi = rssi;
        this.scanRecordBytes = scanRecordBytes;
    }

    public BluetoothScanResult(BluetoothDevice device, int rssi, ScanRecord scanRecord) {
        this.device = device;
        this.rssi = rssi;
        this.scanRecord = scanRecord;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public String getName() {
        String name = getDevice().getName();
        return TextUtils.isEmpty(name) || TextUtils.equals("null", name) ? "Unknown" : name;
    }

    public String getAddress() {
        String address = getDevice().getAddress();
        return TextUtils.isEmpty(address) ? "--" : address;
    }


    public int getBondState() {
        return getDevice().getBondState();
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public byte[] getScanRecordBytes() {
        return scanRecordBytes;
    }

    public void setScanRecordBytes(byte[] scanRecordBytes) {
        this.scanRecordBytes = scanRecordBytes;
    }

    public ScanRecord getScanRecord() {
        return scanRecord;
    }

    public void setScanRecord(ScanRecord scanRecord) {
        this.scanRecord = scanRecord;
    }

}
