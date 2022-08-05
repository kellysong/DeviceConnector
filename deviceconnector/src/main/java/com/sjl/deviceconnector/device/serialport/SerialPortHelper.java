package com.sjl.deviceconnector.device.serialport;

import android.serialport.SerialPortFinder;

import com.sjl.deviceconnector.util.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 串口辅助类
 *
 * @author Kelly
 * @version 1.0.0
 * @filename SerialPortHelper
 * @time 2022/7/25 11:56
 * @copyright(C) 2022 song
 */
public class SerialPortHelper {
    private static List<String> deviceList = new ArrayList<>();

    /**
     * 判断串口是否存在（打开不存在的串口导致阻塞卡死）
     * @param devicePath
     * @return
     */
    public static boolean isExistDevice(String devicePath) {
        getDeviceList();
        return deviceList.contains(devicePath);
    }

    /**
     * 获取串口设备列表
     * @return
     */
    public static List<String> getDeviceList() {
        if (deviceList == null || deviceList.size() == 0){
            SerialPortFinder serialPortFinder = new SerialPortFinder();
            String[] allDevicesPath = serialPortFinder.getAllDevicesPath();
            deviceList.addAll(Arrays.asList(allDevicesPath));
            LogUtils.i("SerialPortDevicesPath:" + Arrays.asList(allDevicesPath).toString());
        }
        return deviceList;
    }
}
