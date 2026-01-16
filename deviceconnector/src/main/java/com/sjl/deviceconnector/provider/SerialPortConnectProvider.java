package com.sjl.deviceconnector.provider;

import android.serialport.SerialPort;

import com.sjl.deviceconnector.ErrorCode;
import com.sjl.deviceconnector.entity.SerialPortConfig;
import com.sjl.deviceconnector.util.LogUtils;

/**
 * 串口连接提供者
 *
 * @author Kelly
 * @version 1.0.0
 * @filename SerialPortConnectProvider
 * @time 2020/10/30 11:31
 * @copyright(C) 2020 song
 */
public class SerialPortConnectProvider extends BaseIoConnectProvider {
    private final SerialPortConfig serialPortConfig;
    private SerialPort mSerialPort;

    /**
     * 初始化串口连接提供者
     *
     * @param serialPortConfig 串口配置
     */
    public SerialPortConnectProvider(SerialPortConfig serialPortConfig) {
        this.serialPortConfig = serialPortConfig;
    }


    @Override
    public int open() {
        int state = getState();
        if (state == ErrorCode.ERROR_OK) {
            return state;
        }
        try {
            mSerialPort = SerialPort.newBuilder(serialPortConfig.getDevice(), serialPortConfig.getBaudRate())
                    .parity(serialPortConfig.getParity())
                    .flags(serialPortConfig.getFlags())
                    .dataBits(serialPortConfig.getDataBits())
                    .stopBits(serialPortConfig.getStopBits()).build();
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
            mConnectState = true;
        } catch (Exception e) {
            LogUtils.e("打开串口失败", e);
            close();
            return ErrorCode.ERROR_FAIL;
        }
        return ErrorCode.ERROR_OK;
    }


    @Override
    public void close() {
        super.close();
        mConnectState = false;
        close(getOutputStream());
        close(getInputStream());
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }




}
