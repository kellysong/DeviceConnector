package com.sjl.deviceconnector.device.bluetooth.scanner;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.text.TextUtils;

import com.sjl.deviceconnector.entity.BluetoothScanResult;
import com.sjl.deviceconnector.util.BluetoothUtils;
import com.sjl.deviceconnector.util.LogUtils;

import java.util.List;


/**
 * ble扫描
 * <p>Android4.3开始，开始支持BLE功能，但只支持Central Mode(中心模式，可连接外围设备)</p>
 * <p>Android5.0开始，开始支持Peripheral Mode(外围模式,可被中心设备连接)</p>
 *
 * @author Kelly
 * @version 1.0.0
 * @filename BluetoothLowEnergyScanner
 * @time 2023/3/3 14:49
 * @copyright(C) 2023 song
 */
public class BluetoothLowEnergyScanner extends AbstractBluetoothScanner {


    @Override
    public void startScan() {
        //android 5.0后
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BluetoothLeScanner bluetoothLeScanner = BluetoothUtils.getBluetoothLeScanner();
            //创建ScanSettings的build对象用于设置参数
            ScanSettings.Builder builder = new ScanSettings.Builder()
                    //设置低功耗模式
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
            //android 6.0添加设置回调类型、匹配模式等
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //定义回调类型
                builder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
                //设置蓝牙LE扫描滤波器硬件匹配的匹配模式
                builder.setMatchMode(ScanSettings.MATCH_MODE_STICKY);
            }

            //芯片组支持批处理芯片上的扫描
            if (BluetoothUtils.getBluetoothAdapter().isOffloadedScanBatchingSupported()) {
                //不开启批处理扫描模式,即不回调onBatchScanResults
                builder.setReportDelay(0L);
            }
            builder.build();
            bluetoothLeScanner.startScan(mScanCallback);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothUtils.getBluetoothAdapter().startLeScan(mLeScanCallback);
        } else {
            throw new RuntimeException("系统版本过低，不支持ble，当前系统版本为:" + Build.VERSION.SDK_INT);
        }

    }

    @Override
    public void stopScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BluetoothLeScanner bluetoothLeScanner = BluetoothUtils.getBluetoothLeScanner();
            bluetoothLeScanner.stopScan(mScanCallback);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothUtils.getBluetoothAdapter().stopLeScan(mLeScanCallback);
        }
        notifyScanFinish();
        removeListener();
    }

    /**
     * 5.0以上扫描回调
     */
    @SuppressLint("NewApi")
    private final ScanCallback mScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothScanResult bluetoothScanResult = new BluetoothScanResult(result.getDevice(), result.getRssi(), result.getScanRecord());
            bluetoothScanResult.setScanRecordBytes(result.getScanRecord().getBytes());

            notifyDeviceFounded(bluetoothScanResult);

            if (!TextUtils.isEmpty(address) && address.equals(bluetoothScanResult.getAddress())) {
                stopScan();
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {

        }

        @Override
        public void onScanFailed(int errorCode) {
            LogUtils.e("扫描失败errorCode：" + errorCode);
        }

    };

    /**
     * 4.3-5.0以下扫描回调
     */
    @SuppressLint("NewApi")
    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            BluetoothScanResult bluetoothScanResult = new BluetoothScanResult(device, rssi, scanRecord);

            notifyDeviceFounded(bluetoothScanResult);

            if (!TextUtils.isEmpty(address) && address.equals(bluetoothScanResult.getAddress())) {
                stopScan();
            }
        }

    };
}
