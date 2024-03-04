package com.sjl.deviceconnector.test.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import com.sjl.core.util.log.LogUtils;
import com.sjl.deviceconnector.test.R;
import com.sjl.deviceconnector.test.app.MyApplication;
import com.sjl.deviceconnector.test.entity.ConnectWay;
import com.sjl.deviceconnector.test.entity.MessageEvent;
import com.sjl.deviceconnector.test.util.MessageEventUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

import androidx.annotation.Nullable;

/**
 * TODO
 *
 * @author Kelly
 * @version 1.0.0
 * @filename WifiService
 * @time 2022/7/26 11:54
 * @copyright(C) 2022 song
 */
public class WifiService extends Service {

    private ServerThread mServerThread;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private String notificationId = "Wifi";
    private String notificationName = "WifiService";

    private void showNotification(){
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)//通知的图片
                .setContentTitle("Wifi服务")
                .setContentText("正在运行...");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(notificationId);
        }
        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(notificationId, notificationName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        startForeground(2,notification);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);

        //防止被杀死
        showNotification();
        mServerThread = new ServerThread();
        mServerThread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        for (WifiService.ReadThread readThread : mClients) {
            readThread.close();
        }
        mClients.clear();
        MessageEventUtils.sendLog("WiFi服务停止运行");

        if (mServerThread != null) {
            mServerThread.close();
        }
    }
    private final CopyOnWriteArrayList<ReadThread> mClients = new CopyOnWriteArrayList<>();

    /**
     * 服务端：监听客户端连接线程
     */
    private class ServerThread extends Thread {
        private ServerSocket mServerSocket;

        public ServerThread() {

        }

        public void run() {
            Socket socket;
            try {
                mServerSocket = new ServerSocket(MyApplication.getWifiInfo().getPort());
                MessageEventUtils.sendLog("wifi服务启动成功");
                while (true) {
                    socket = mServerSocket.accept();
                    if (socket != null) {
                        remove(socket.getInetAddress().getHostAddress());
                        ReadThread readThread = new ReadThread(socket);
                        readThread.start();
                        mClients.add(readThread);
                    }

                }
            } catch (Exception e) {
                LogUtils.e("监听客户端连接异常", e);
                close();
            }
        }
        public void remove(String host) {
            ReadThread thread = null;
            for (ReadThread readThread : mClients) {
                if (readThread.host.equals(host)){
                    readThread.close();
                    thread = readThread;
                    break;
                }
            }
            if (thread != null){
                mClients.remove(thread);
            }
        }

        public void close() {
            for (ReadThread readThread : mClients) {
                readThread.close();
            }
            mClients.clear();
            try {
                if (mServerSocket != null) {
                    mServerSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 服务端：读取客户端数据
     */
    private class ReadThread extends Thread {
        private final Socket mSocket;
        private String host;
        private InputStream is;
        private OutputStream out;
        private boolean stop;

        public ReadThread(Socket socket) {
            this.mSocket = socket;
            this.stop = true;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int len;
            try {
                host = mSocket.getInetAddress().getHostAddress();
                LogUtils.i("连接的客户端：" + mSocket.toString());
                is = mSocket.getInputStream();
                out = mSocket.getOutputStream();
                while (stop) {
                   synchronized (this){
                       if ((len = is.read(buffer)) > 0) {
                           byte[] resultData = new byte[len];
                           System.arraycopy(buffer, 0, resultData, 0, len);

                           MessageEventUtils.sendLog("服务端收到客户端发送的数据：" + new String(resultData));
                           String callData = System.currentTimeMillis() + "收到你的数据了";
                           send(callData.getBytes());
                       }
                   }
                }
            } catch (Exception e) {
//                LogUtils.e("读取客户端数据异常", e);
                close();
            }finally {
                MessageEventUtils.sendLog(host+"断开连接");
            }
        }

        public synchronized void send(byte[] bytes) throws IOException {
            out.write(bytes);
            out.flush();
        }

        public void close() {
            this.stop = false;
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (mSocket != null) {
                    mSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.getCode() == ConnectWay.WIFI.ordinal()) {
            String data = (String) event.getEvent();
            for (WifiService.ReadThread readThread : mClients) {
                try {
                    readThread.send(data.getBytes());
                    MessageEventUtils.sendLog("发送数据给客户端成功");
                } catch (Exception e) {
                    MessageEventUtils.sendLog("发送数据给客户端异常：" + e.getMessage());
                }
            }
        }
    }

}
