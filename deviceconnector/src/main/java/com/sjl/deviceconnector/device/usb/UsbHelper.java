package com.sjl.deviceconnector.device.usb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.sjl.deviceconnector.DeviceContext;
import com.sjl.deviceconnector.listener.ConnectedListener;
import com.sjl.deviceconnector.listener.ReceiverObservable;
import com.sjl.deviceconnector.listener.UsbPermissionListener;
import com.sjl.deviceconnector.listener.UsbPlugListener;
import com.sjl.deviceconnector.util.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * usb辅助类，设备管理，权限管理，插拔管理
 *
 * @author Kelly
 * @version 1.0.0
 * @filename UsbHelper
 * @time 2022/6/15 17:30
 * @copyright(C) 2022 song
 */
public class UsbHelper implements ReceiverObservable {

    private static final String INTENT_ACTION_GRANT_USB = "com.sjl.deviceconnector.GRANT_USB";
    private final static String ACTION_USB_STATE = "android.hardware.usb.action.USB_STATE";

    private BroadcastReceiver mBroadcastReceiver;
    private ConnectedListener<UsbDevice> connectedListener;
    private UsbPermissionListener usbPermissionListener;
    private UsbPlugListener usbPlugListener;

    private UsbHelper() {
    }


    public static UsbHelper getInstance() {
        return UsbHelperHolder.singleton;
    }



    private static final class UsbHelperHolder {
        private static UsbHelper singleton = new UsbHelper();
    }


    /**
     * 连接状态监听
     * @param connectedListener
     */
    public void setConnectedListener(ConnectedListener<UsbDevice> connectedListener) {
        this.connectedListener = connectedListener;
    }

    /**
     * Usb插拔监听
     *
     * @param usbPlugListener
     */
    public void setUsbPlugListener(UsbPlugListener usbPlugListener) {
        this.usbPlugListener = usbPlugListener;
    }

    /**
     * 申请Usb设备权限
     *
     * @param vendorId  产商id
     * @param productId 产品id
     */
    public void requestPermission(int vendorId, int productId) {
        UsbDevice usbDevice = getDevice(vendorId, productId);
        requestPermission(usbDevice, null);
    }

    /**
     * 申请Usb设备权限
     *
     * @param usbDevice
     */
    public void requestPermission(UsbDevice usbDevice) {
        requestPermission(usbDevice, null);
    }

    /**
     * 申请Usb设备权限,需要先注册广播
     *
     * @param usbDevice
     * @param usbPermissionListener
     */
    public void requestPermission(UsbDevice usbDevice, UsbPermissionListener usbPermissionListener) {
        if (usbDevice == null) {
            throw new NullPointerException("usbDevice is null.");
        }
        this.usbPermissionListener = usbPermissionListener;
        if (hasPermission(usbDevice)) {
            if (this.usbPermissionListener != null) {
                this.usbPermissionListener.onGranted(usbDevice);
            }
            return;
        }
        UsbManager usbManager = (UsbManager) DeviceContext.getContext().getSystemService(Context.USB_SERVICE);
        PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(DeviceContext.getContext(), 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
        usbManager.requestPermission(usbDevice, usbPermissionIntent);
    }

    public boolean hasPermission(UsbDevice usbDevice) {
        UsbManager usbManager = (UsbManager) DeviceContext.getContext().getSystemService(Context.USB_SERVICE);
        return usbManager.hasPermission(usbDevice);
    }

    /**
     * 根据产商id和产品id获取UsbDevice
     *
     * @param vendorId  产商id
     * @param productId 产品id
     * @return
     */
    public static UsbDevice getDevice(int vendorId, int productId) {
        List<UsbDevice> deviceList = getDeviceList();
        for (UsbDevice usbDevice : deviceList) {
            if (usbDevice.getVendorId() == vendorId && usbDevice.getProductId() == productId) {
                return usbDevice;
            }
        }
        return null;
    }

    /**
     * 获取所有Usb设备列表
     *
     * @return
     */
    public static List<UsbDevice> getDeviceList() {
        UsbManager usbManager = (UsbManager) DeviceContext.getContext().getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        for (UsbDevice usbDevice : deviceList.values()) {
            LogUtils.i("vendorId:" + usbDevice.getVendorId() + ",productId:" + usbDevice.getProductId());
        }
        return new ArrayList<>(deviceList.values());
    }



    @Override
    public void registerReceiver() {
        Context context = DeviceContext.getContext();
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_ACTION_GRANT_USB);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(ACTION_USB_STATE);
        mBroadcastReceiver =  new MyBroadcastReceiver();
        context.registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    public void unregisterReceiver() {
        Context context = DeviceContext.getContext();
        if (mBroadcastReceiver != null) {
            context.unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
        removeListener();
    }


    public void removeListener() {
        connectedListener = null;
        usbPermissionListener = null;
        usbPlugListener = null;
    }

    private final class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (INTENT_ACTION_GRANT_USB.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (usbPermissionListener != null) {
                        usbPermissionListener.onGranted(device);
                    }
                } else {
                    LogUtils.w("usb 授权拒绝： " + device.getDeviceName());
                    if (usbPermissionListener != null) {
                        usbPermissionListener.onDenied(device);
                    }
                }

            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                LogUtils.i("USB插入:" + device.toString());
                if (usbPlugListener != null) {
                    usbPlugListener.onAttached(device);
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                LogUtils.w("USB拔出:" + device.toString());
                if (usbPlugListener != null) {
                    usbPlugListener.onDetached(device);
                }
            } else if (ACTION_USB_STATE.equals(action)) {
                boolean connected = intent.getExtras().getBoolean("connected");
                List<UsbDevice> deviceList = getDeviceList();
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                LogUtils.w("connected:" + connected + ",设备数：" + deviceList.size());
                if (connectedListener != null){
                    connectedListener.onResult(device,connected);
                }
            }
        }
    };


}
