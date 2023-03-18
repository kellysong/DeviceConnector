package com.sjl.deviceconnector.provider;

import android.os.Build;

import com.sjl.deviceconnector.ErrorCode;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Socket连接提供者,可作为TCP通讯使用
 *
 * @author Kelly
 * @version 1.0.0
 * @filename SocketConnectProvider.java
 * @time 2018/4/13 8:40
 * @copyright(C) 2018 song
 */
public class SocketConnectProvider extends BaseIoConnectProvider {
    private Socket mSocket;
    private String ip;
    private int port;
    private int connectTimeout, readTimeout;


    /**
     * 初始化Socket连接提供者
     *
     * @param ip           ip地址
     * @param port           端口号
     * @param connectTimeout 连接超时时间，毫秒
     * @param readTimeout    读取超时时间，毫秒
     */
    public SocketConnectProvider(String ip, int port, int connectTimeout, int readTimeout) {
        this.ip = ip;
        this.port = port;
        this.connectTimeout = connectTimeout < 0 ? 10 * 1000 : connectTimeout;
        this.readTimeout = readTimeout < 0 ? 10 * 1000 : readTimeout;
    }




    @Override
    public int open() {
        int state = getState();
        if (state == ErrorCode.ERROR_OK) {
            return state;
        }
        mSocket = new Socket();
        SocketAddress address = new InetSocketAddress(ip, port);//socket连接地址
        try {
            mSocket.connect(address, connectTimeout);
            mSocket.setSoTimeout(readTimeout);
            mInputStream = mSocket.getInputStream();
            mOutputStream = mSocket.getOutputStream();
            mConnectState = true;
            return ErrorCode.ERROR_OK;
        } catch (Exception e) {
            close();
            return ErrorCode.ERROR_FAIL;
        }

    }

    @Override
    public void close() {
        mConnectState = false;
        close(getOutputStream());
        close(getInputStream());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            close(mSocket);
        }
    }

}
