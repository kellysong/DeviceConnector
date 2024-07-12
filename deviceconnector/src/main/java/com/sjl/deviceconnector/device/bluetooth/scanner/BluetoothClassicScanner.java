package com.sjl.deviceconnector.device.bluetooth.scanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.sjl.deviceconnector.DeviceContext;
import com.sjl.deviceconnector.entity.BluetoothScanResult;
import com.sjl.deviceconnector.listener.ReceiverObservable;
import com.sjl.deviceconnector.util.BluetoothUtils;
import com.sjl.deviceconnector.util.LogUtils;

import java.util.List;

/**
 * 经典蓝牙扫描
 *
 * @author Kelly
 * @version 1.0.0
 * @filename BluetoothClassicScanner
 * @time 2023/3/3 14:47
 * @copyright(C) 2023 song
 */
public class BluetoothClassicScanner extends AbstractBluetoothScanner implements ReceiverObservable {
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    public void startScan() {
        List<BluetoothDevice> bondedDevices = BluetoothUtils.getBondedDevices();
        for (BluetoothDevice bluetoothDevice : bondedDevices) {
            if (bluetoothDevice.getAddress().equals(address)) {
                notifyDeviceFounded(new BluetoothScanResult(bluetoothDevice,-1, (byte[]) null));
                notifyScanFinish();
                break;
            }
        }
        registerReceiver();
        BluetoothAdapter bluetoothAdapter = BluetoothUtils.getBluetoothAdapter();
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
    }

    @Override
    public void stopScan() {
        unregisterReceiver();
        BluetoothAdapter bluetoothAdapter = BluetoothUtils.getBluetoothAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        notifyScanFinish();
        removeListener();
    }



    @Override
    public void registerReceiver() {
        Context context = DeviceContext.getContext();
        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_FOUND);// 搜索蓝压设备，每搜到一个设备发送一条广播
        intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);// 扫描完成
        intent.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intent.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
//        intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);// 配对开始时，配对成功时
//        intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);// 搜索模式改变
//        intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);// 蓝牙开关状态
        mBroadcastReceiver = new ScanBroadcastReceiver();
        context.registerReceiver(mBroadcastReceiver, intent);
    }


    @Override
    public void unregisterReceiver() {
        Context context = DeviceContext.getContext();
        if (mBroadcastReceiver != null) {
            context.unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
        address = null;
    }


    private final class ScanBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getExtras().get(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);

                LogUtils.i("BluetoothDevice:" + device.getName() + ",address:" + device.getAddress());
                notifyDeviceFounded(new BluetoothScanResult(device, rssi, (byte[]) null));

                if (!TextUtils.isEmpty(address) && address.equals(device.getAddress())) {
                    stopScan();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                LogUtils.w("蓝牙扫描完毕");
                notifyScanFinish();
            }
        }
    }
}
