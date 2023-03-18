package com.sjl.deviceconnector.device.bluetooth.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Build;

import com.sjl.deviceconnector.util.LogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.RequiresApi;

/**
 * TODO
 *
 * @author Kelly
 * @version 1.0.0
 * @filename BluetoothGattWrap
 * @time 2023/3/4 12:56
 * @copyright(C) 2023 song
 */
public class BluetoothGattWrap {
    /**
     * 服务列表
     */
    private List<BluetoothGattService> mGattServiceList = new ArrayList<>();
    /**
     * 服务id和特征映射列表
     */
    private Map<UUID, Map<UUID, BluetoothGattCharacteristic>> mDeviceProfile = new LinkedHashMap<>();

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void addServices(List<BluetoothGattService> services) {
        mGattServiceList.clear();
        mGattServiceList.addAll(services);
        Map<UUID, Map<UUID, BluetoothGattCharacteristic>> newProfiles = new HashMap<UUID, Map<UUID, BluetoothGattCharacteristic>>();

        for (BluetoothGattService service : services) {
            UUID serviceUUID = service.getUuid();
            Map<UUID, BluetoothGattCharacteristic> map = newProfiles.get(serviceUUID);

            if (map == null) {
                LogUtils.i("发现 service uuid " + serviceUUID);
                map = new HashMap<UUID, BluetoothGattCharacteristic>();
                newProfiles.put(service.getUuid(), map);
            }

            List<BluetoothGattCharacteristic> characters = service.getCharacteristics();

            for (BluetoothGattCharacteristic character : characters) {
                UUID characterUUID = character.getUuid();
                LogUtils.i("character uuid : " + characterUUID);
                map.put(character.getUuid(), character);
            }
        }
        mDeviceProfile.clear();
        mDeviceProfile.putAll(newProfiles);
    }

    /**
     * 根据服务id获取 BluetoothGattService
     *
     * @param serviceId
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public BluetoothGattService getService(UUID serviceId) {
        if (serviceId == null) {
            return null;
        }

        List<BluetoothGattService> services = getServices();
        for (BluetoothGattService service : services) {
            if (service.getUuid().equals(serviceId)) {
                return service;
            }
        }
        return null;
    }

    /**
     * 根据服务id和特征id获取BluetoothGattCharacteristic
     *
     * @param serviceId
     * @param characterId
     * @return
     */
    public BluetoothGattCharacteristic getCharacter(UUID serviceId, UUID characterId) {
        BluetoothGattCharacteristic characteristic = null;

        Map<UUID, BluetoothGattCharacteristic> characters = mDeviceProfile.get(serviceId);
        if (characters != null) {
            characteristic = characters.get(characterId);
        }
        return characteristic;
    }

    /**
     * 先根据服务id和特征id获取BluetoothGattCharacteristic，没有再从bluetoothGatt获取
     *
     * @param serviceId
     * @param characterId
     * @param bluetoothGatt
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public BluetoothGattCharacteristic getCharacter(UUID serviceId, UUID characterId, BluetoothGatt bluetoothGatt) {
        BluetoothGattCharacteristic characteristic = getCharacter(serviceId, characterId);
        if (characteristic == null) {
            if (bluetoothGatt != null) {
                BluetoothGattService gattService = bluetoothGatt.getService(serviceId);
                if (gattService != null) {
                    characteristic = gattService.getCharacteristic(characterId);
                }
            }
        }
        return characteristic;
    }


    public List<BluetoothGattService> getServices() {
        return mGattServiceList;
    }
}
