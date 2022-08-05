package com.sjl.deviceconnector.device.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.sjl.deviceconnector.DeviceContext;
import com.sjl.deviceconnector.listener.BluetoothScanListener;
import com.sjl.deviceconnector.listener.ConnectedListener;
import com.sjl.deviceconnector.listener.ReceiverObservable;
import com.sjl.deviceconnector.util.LogUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 蓝牙辅助类
 *
 * @author Kelly
 * @version 1.0.0
 * @filename BluetoothHepler
 * @time 2022/7/26 15:55
 * @copyright(C) 2022 song
 */
public class BluetoothHelper implements ReceiverObservable {
    private BroadcastReceiver mBroadcastReceiver;
    private String address;
    private BluetoothScanListener bluetoothScanListener;
    private ConnectedListener<BluetoothDevice> connectedListener;

    private BluetoothHelper() {
    }

    public static BluetoothHelper getInstance() {
        return BluetoothHelperHolder.singleton;
    }


    private static final class BluetoothHelperHolder {
        private static BluetoothHelper singleton = new BluetoothHelper();
    }

    /**
     * 连接状态监听
     *
     * @param connectedListener
     */
    public void setConnectedListener(ConnectedListener<BluetoothDevice> connectedListener) {
        this.connectedListener = connectedListener;
    }


    /**
     * 蓝牙适配器
     *
     * @return
     */
    public static BluetoothAdapter getBluetoothAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * 蓝牙扫描,需要先注册广播
     *
     * @param bluetoothScanListener
     */
    public void startScan(BluetoothScanListener bluetoothScanListener) {
        startScan(null, bluetoothScanListener);
    }

    /**
     * 根据蓝牙地址扫描指定设备
     *
     * @param address
     * @param bluetoothScanListener
     */
    public void startScan(String address, BluetoothScanListener bluetoothScanListener) {
        List<BluetoothDevice> bondedDevices = getBondedDevices();
        for (BluetoothDevice bluetoothDevice : bondedDevices) {
            if (bluetoothDevice.getAddress().equals(address)) {
                if (bluetoothScanListener != null) {
                    bluetoothScanListener.onDeviceFound(bluetoothDevice);
                }
                if (bluetoothScanListener != null) {
                    bluetoothScanListener.onScanFinish();
                }
                break;
            }
        }
        this.address = address;
        this.bluetoothScanListener = bluetoothScanListener;
        getBluetoothAdapter().startDiscovery();
    }

    /**
     * 判断蓝牙开关是否启用
     *
     * @return
     */
    public static boolean isEnabled() {
        return getBluetoothAdapter().isEnabled();
    }

    /**
     * 打开蓝牙
     */
    public static void enable() {
        getBluetoothAdapter().enable();
    }

    /**
     * 关闭蓝牙
     */
    public static void disable() {
        getBluetoothAdapter().disable();
    }

    /**
     * 取消扫描
     */
    public void stopScan() {
        if (getBluetoothAdapter().isDiscovering()) {
            getBluetoothAdapter().cancelDiscovery();
        }
        bluetoothScanListener = null;
    }


    /**
     * 获取已配对的蓝牙设备
     *
     * @return
     */
    public static List<BluetoothDevice> getBondedDevices() {
        Set<BluetoothDevice> bondedDevices = getBluetoothAdapter().getBondedDevices();
        if (bondedDevices == null) {
            return Collections.emptyList();
        }
        return new ArrayList(bondedDevices);
    }


    /**
     * 取消配对
     *
     * @param device
     * @return
     * @throws Exception
     */
    public static boolean cancelBond(BluetoothDevice device) throws Exception {
        Method createBondMethod = device.getClass().getMethod("removeBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
        return returnValue.booleanValue();
    }

    /**
     * 配对蓝牙设备
     *
     * @param device
     * @return
     * @throws Exception
     */
    public static boolean createBond(BluetoothDevice device) throws Exception {
        Method createBondMethod = device.getClass().getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
        return returnValue.booleanValue();
    }

    private void removeListener() {
        connectedListener = null;
        bluetoothScanListener = null;
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
        mBroadcastReceiver = new MyBroadcastReceiver();
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
        removeListener();
    }


    private final class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getExtras().get(BluetoothDevice.EXTRA_DEVICE);
                LogUtils.i("BluetoothDevice:" + device.getName() + ",address:" + device.getAddress());
                if (bluetoothScanListener != null) {
                    if (!TextUtils.isEmpty(address) && address.equals(device.getAddress())) {
                        stopScan();
                    }
                    bluetoothScanListener.onDeviceFound(device);
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                LogUtils.w("蓝牙扫描完毕");
                if (bluetoothScanListener != null) {
                    bluetoothScanListener.onScanFinish();
                }
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name = device.getName();
                LogUtils.i("发现蓝牙设备连接成功，name:" + name);
                if (connectedListener != null) {
                    connectedListener.onResult(device, true);
                }

            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name = device.getName();
                LogUtils.w("发现蓝牙设备断开连接，name:" + name);
                if (connectedListener != null) {
                    connectedListener.onResult(device, false);
                }
            }
        }
    }
}
