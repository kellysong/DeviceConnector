package com.sjl.deviceconnector.test.service;



import com.sjl.core.util.log.LogUtils;
import com.sjl.deviceconnector.provider.BaseIoConnectProvider;
import com.sjl.deviceconnector.test.util.MessageEventUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * 客户端监听服务端数据的线程
 *<p>注意：单独监听就不能使用baseConnectProvider.read接口</p>
 * @author Kelly
 * @version 1.0.0
 * @filename ReadThread
 * @time 2024/3/4 18:23
 * @copyright(C) 2024 song
 */
public class ReadThread extends Thread {
    private BaseIoConnectProvider connectProvider;
    private boolean running;

    public ReadThread(BaseIoConnectProvider connectProvider) {
        this.connectProvider = connectProvider;
    }

    @Override
    public void run() {
        running = true;
        LogUtils.i("Start read thread.");
        InputStream inputStream = connectProvider.getInputStream();
        while (running) {
            if (inputStream == null) {
                return;
            }
            try {
                byte[] readData = new byte[128];
                int size = inputStream.read(readData);
                if (size > 0) {
                    synchronized (this){
                        byte[] tempBuffer = new byte[size];
                        System.arraycopy(readData, 0, tempBuffer, 0, tempBuffer.length);
                        MessageEventUtils.sendLog("收到服务端数据：" + new String(tempBuffer));
                    }
                }
            } catch (Exception e) {
                LogUtils.e("Data read exception", e);
            }
        }
        LogUtils.w("The read thread stop.");
    }

    public void close(){
        running = false;
        try {
            connectProvider.getInputStream().close();
        } catch (IOException e) {

        }
    }
}

