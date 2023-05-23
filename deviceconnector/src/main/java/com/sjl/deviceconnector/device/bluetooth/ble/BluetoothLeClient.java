package com.sjl.deviceconnector.device.bluetooth.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Build;

import com.sjl.deviceconnector.util.LogUtils;

import java.util.UUID;


/**
 * Ble客户端实现
 *
 * @author Kelly
 * @version 1.0.0
 * @filename BluetoothLeClient
 * @time 2023/3/4 13:52
 * @copyright(C) 2023 song
 */
public class BluetoothLeClient implements IBluetoothLeClient {
    public static final byte[] EMPTY_BYTES = new byte[]{};
    /**
     * 系统提供接受通知自带的UUID
     */
    public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private BluetoothGattWrap mBluetoothGattWrap;
    private BluetoothGatt mBluetoothGatt;

    public BluetoothLeClient(BluetoothGattWrap mBluetoothGattWrap) {
        this.mBluetoothGattWrap = mBluetoothGattWrap;
    }

    public void setBluetoothGatt(BluetoothGatt bluetoothGatt) {
        this.mBluetoothGatt = bluetoothGatt;
    }

    @Override
    public boolean readCharacteristic(UUID serviceId, UUID characterId) {
        BluetoothGattCharacteristic characteristic = mBluetoothGattWrap.getCharacter(serviceId, characterId);

        if (characteristic == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (!mBluetoothGatt.readCharacteristic(characteristic)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean writeCharacteristic(UUID serviceId, UUID characterId, byte[] value) {
        BluetoothGattCharacteristic characteristic = mBluetoothGattWrap.getCharacter(serviceId, characterId);

        if (characteristic == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            characteristic.setValue(value != null ? value : EMPTY_BYTES);
            if (!mBluetoothGatt.writeCharacteristic(characteristic)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean readDescriptor(UUID serviceId, UUID characterId, UUID descriptorId) {
        BluetoothGattCharacteristic characteristic = mBluetoothGattWrap.getCharacter(serviceId, characterId);

        if (characteristic == null) {
            return false;
        }

        BluetoothGattDescriptor gattDescriptor;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            gattDescriptor = characteristic.getDescriptor(descriptorId);
            if (gattDescriptor == null) {
                LogUtils.e("gattDescriptor为空");
                return false;
            }
            if (!mBluetoothGatt.readDescriptor(gattDescriptor)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean writeDescriptor(UUID serviceId, UUID characterId, UUID descriptorId, byte[] value) {
        BluetoothGattCharacteristic characteristic = mBluetoothGattWrap.getCharacter(serviceId, characterId);

        if (characteristic == null) {
            return false;
        }

        BluetoothGattDescriptor gattDescriptor;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            gattDescriptor = characteristic.getDescriptor(descriptorId);
            if (gattDescriptor == null) {
                LogUtils.e("gattDescriptor为空");
                return false;
            }
            gattDescriptor.setValue(value != null ? value : EMPTY_BYTES);
            if (!mBluetoothGatt.writeDescriptor(gattDescriptor)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean setCharacteristicNotification(UUID serviceId, UUID characterId, boolean enable) {
        BluetoothGattCharacteristic characteristic = mBluetoothGattWrap.getCharacter(serviceId, characterId);
        if (characteristic == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (!mBluetoothGatt.setCharacteristicNotification(characteristic, enable)) {
                LogUtils.e("setCharacteristicNotification失败");
                return false;
            }
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);

            if (descriptor == null) {
                LogUtils.e("获取自带的descriptor为空");
                return false;
            }

            byte[] value = enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;

            if (!descriptor.setValue(value)) {
                return false;
            }

            if (!mBluetoothGatt.writeDescriptor(descriptor)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean setCharacteristicIndication(UUID serviceId, UUID characterId, boolean enable) {
        BluetoothGattCharacteristic characteristic = mBluetoothGattWrap.getCharacter(serviceId, characterId);
        if (characteristic == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (!mBluetoothGatt.setCharacteristicNotification(characteristic, enable)) {
                LogUtils.e("setCharacteristicNotification失败");
                return false;
            }
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);

            if (descriptor == null) {
                LogUtils.e("获取自带的descriptor为空");
                return false;
            }

            byte[] value = enable ? BluetoothGattDescriptor.ENABLE_INDICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;

            if (!descriptor.setValue(value)) {
                return false;
            }

            if (!mBluetoothGatt.writeDescriptor(descriptor)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean readRemoteRssi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (!mBluetoothGatt.readRemoteRssi()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean requestMtu(int mtu) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!mBluetoothGatt.requestMtu(mtu)) {
                return false;
            }
        }
        return true;
    }


}
