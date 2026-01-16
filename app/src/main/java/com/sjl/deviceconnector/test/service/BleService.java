package com.sjl.deviceconnector.test.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;

import com.sjl.core.util.log.LogUtils;
import com.sjl.deviceconnector.test.BuildConfig;
import com.sjl.deviceconnector.test.R;
import com.sjl.deviceconnector.test.entity.ConnectWay;
import com.sjl.deviceconnector.test.entity.MessageEvent;
import com.sjl.deviceconnector.test.util.MessageEventUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.UUID;

import androidx.annotation.Nullable;

/**
 * 低功耗蓝牙
 *
 * @author Kelly
 * @version 1.0.0
 * @filename BleService
 * @time 2024/3/1 17:55
 * @copyright(C) 2024 song
 */
public class BleService extends Service {
    //蓝牙适配器
    BluetoothAdapter mBluetoothAdapter;
    //蓝牙广播
    private BluetoothLeAdvertiser mAdvertiser;
    //蓝牙Server
    private BluetoothGattServer mBluetoothGattServer;
    //蓝牙服务
    private BluetoothGattService mService;
    //服务UUID
    public static final UUID UUID_SERVICE = UUID.fromString(BuildConfig.uuid_service);
    //添加可读+通知characteristicD
    public static final UUID UUID_CHAR_READ_NOTIFY = UUID.fromString(BuildConfig.uuid_character_read);
    public static final UUID UUID_DESC_NOTITY = UUID.fromString("000051234-0000-1000-8000-00805f9b34fb");

    //添加可写characteristic
    public static final UUID UUID_CHAR_WRITE = UUID.fromString(BuildConfig.uuid_character_write);
    /**
     * 当前连接设备
     */
    private BluetoothDevice bluetoothDevice;
    byte[] responseData = new byte[]{0x01, 0x02, 0x03};
    private BluetoothGattCharacteristic characteristicRead;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final String notificationId = "Ble";
    private final String notificationName = "BleService";

    private void showNotification() {
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)//通知的图片
                .setContentTitle("Ble服务")
                .setContentText("正在运行...");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(notificationId);
        }
        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(notificationId, notificationName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        startForeground(1, notification);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        showNotification();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!mBluetoothAdapter.isEnabled()) {
                        SystemClock.sleep(200);
                    }
                    startBluetoothServer();
                }
            }).start();
            return;
        }
        startBluetoothServer();
    }

    private void startBluetoothServer() {
        boolean b = initService();
        if (b) {
            //启动BLE蓝牙广播，其它设备才能发现并连接BLE服务端
            startAdvertising();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        MessageEventUtils.sendLog("ble服务停止运行");
        closeBle();
    }

    /**
     * 开启ble广播
     */
    private void startAdvertising() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //广播设置(必须)
            AdvertiseSettings settings = new AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                    .setConnectable(true)
                    .setTimeout(0) //设置广播的最长时间 ,0表示一直广播
                    .build();
            byte[] tagBytes = new byte[0];

            //广播数据(必须，广播启动就会发送)
            AdvertiseData.Builder builder = new AdvertiseData.Builder()
                    .setIncludeDeviceName(true);//包含蓝牙名称
//                    .setIncludeTxPowerLevel(true);//包含发射功率级别
//            .addServiceData(new ParcelUuid(UUID_SERVICE),tagBytes) //服务数据，自定义

            if (tagBytes.length > 0) {
                builder.addManufacturerData(0x1234, tagBytes); //设备厂商数据，自定义
            }
            AdvertiseData advertiseData = builder.build();


            mAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
            if (mAdvertiser != null) {
                mAdvertiser.startAdvertising(settings, advertiseData, advertiseCallback);
            } else {
                LogUtils.w("广播失败，mAdvertiser为空");
            }
        }

    }

    private void stopAdvertising() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mAdvertiser != null) {
                mAdvertiser.stopAdvertising(advertiseCallback);
            }

        }
    }

    @SuppressLint("NewApi")
    private final AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            LogUtils.i("==========开启ble广播成功");
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            LogUtils.e("==========开启ble广播失败，errorCode：" + errorCode);
        }
    };

    @SuppressLint("NewApi")
    private boolean initService() {
        mService = new BluetoothGattService(UUID_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        //添加可读+通知characteristic
        characteristicRead = new BluetoothGattCharacteristic(UUID_CHAR_READ_NOTIFY,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_READ);
        characteristicRead.addDescriptor(new BluetoothGattDescriptor(UUID_DESC_NOTITY, BluetoothGattCharacteristic.PERMISSION_WRITE));
        mService.addCharacteristic(characteristicRead);

        //添加可写characteristic
        BluetoothGattCharacteristic characteristicWrite = new BluetoothGattCharacteristic(UUID_CHAR_WRITE,
                BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE);
        mService.addCharacteristic(characteristicWrite);


        BluetoothManager bluetoothManager = (BluetoothManager) getApplication().getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothGattServer = bluetoothManager.openGattServer(getApplication(), new BluetoothGattServerCallback() {
            @Override
            public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                super.onConnectionStateChange(device, status, newState);
                //连接状态发生变化回调
                LogUtils.w("连接状态发生改变,deviceName=" + device.getName() + ",address=" + device.getAddress() + ",status=" + status + ",newState=" + newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    LogUtils.i("deviceName=" + device.getName() + ",连接成功");
                    BleService.this.bluetoothDevice = device;
                    MessageEventUtils.sendLog("deviceName=" + device.getName() + ",address=" + device.getAddress() + ",连接成功");
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    LogUtils.w("deviceName=" + device.getName() + ",断开连接");
                    MessageEventUtils.sendLog("deviceName=" + device.getName() + ",address=" + device.getAddress() + ",断开连接");
                    BleService.this.bluetoothDevice = null;
                }
            }


            @Override
            public void onServiceAdded(int status, BluetoothGattService service) {
                super.onServiceAdded(status, service);
                LogUtils.i("服务添加成功");
                MessageEventUtils.sendLog("ble服务启动成功");
            }

            @Override
            public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
                UUID uuid = characteristic.getUuid();
                if (UUID_CHAR_READ_NOTIFY.equals(uuid)) {
                    mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
                } else {
                    mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, offset, null);
                }

            }


            @Override
            public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
                UUID uuid = characteristic.getUuid();
                if (UUID_CHAR_WRITE.equals(uuid)) {
                    MessageEventUtils.sendLog("收到客户端数据：" + new String(value));
                    mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);


                } else {
                    mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, offset, value);

                }

            }

            @Override
            public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
                super.onDescriptorReadRequest(device, requestId, offset, descriptor);
                LogUtils.i("远程设备读取本地描述");
                mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
            }


            @Override
            public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
                LogUtils.i("远程设备写入本地描述:" + value);
                mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
            }


            @Override
            public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
                super.onExecuteWrite(device, requestId, execute);
                LogUtils.i("执行所有挂起的写操作");
                mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
            }


            @Override
            public void onNotificationSent(BluetoothDevice device, int status) {
                super.onNotificationSent(device, status);
                LogUtils.i("通知发送成功");
            }


            @Override
            public void onMtuChanged(BluetoothDevice device, int mtu) {
                super.onMtuChanged(device, mtu);
                MessageEventUtils.sendLog("MTU发生更改：" + mtu);
            }

        });

        if (mBluetoothGattServer == null) {
            LogUtils.w("ble服务创建失败，mBluetoothGattServer为空");
            return false;
        }
        //将服务加入到周边
        return mBluetoothGattServer.addService(mService);

    }


    @SuppressLint("NewApi")
    public void closeBle() {
        stopAdvertising();
        if (mBluetoothGattServer != null) {
            mBluetoothGattServer.close();
        }
        LogUtils.i("停止Ble蓝牙服务");
    }

    @SuppressLint("NewApi")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.getCode() == ConnectWay.BLUETOOTH_LOW_ENERGY.ordinal()) {
            String data = (String) event.getEvent();
            if (characteristicRead != null && bluetoothDevice != null) {
                final byte[] newValue = data.getBytes() == null ? responseData : data.getBytes();
                characteristicRead.setValue(newValue);
                boolean success = mBluetoothGattServer.notifyCharacteristicChanged(bluetoothDevice, characteristicRead, false);
                MessageEventUtils.sendLog("发送数据给客户端success：" + success);
            }

        }
    }
}
