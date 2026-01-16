package com.sjl.deviceconnector.provider;


import android.os.SystemClock;
import android.util.Printer;

import com.sjl.deviceconnector.ErrorCode;
import com.sjl.deviceconnector.Waiter;
import com.sjl.deviceconnector.exception.ProviderTimeoutException;
import com.sjl.deviceconnector.listener.DataReceivedListener;
import com.sjl.deviceconnector.util.ByteUtils;
import com.sjl.deviceconnector.util.LogUtils;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * IO通用方法基类
 *
 * @author Kelly
 */
public abstract class BaseIoConnectProvider extends BaseConnectProvider {

    public static final int NORMAL = 0;
    public static final int SPLICING = 1;
    private int readType = NORMAL;
    private ReadThread mReadThread;
    private boolean running = false;
    private byte[] readBytes = null;
    private byte[] tempReadBytes = null;

    private Object obj = new Object();
    private final Waiter waiter = new Waiter();
    protected InputStream mInputStream;
    protected OutputStream mOutputStream;
    private int readBufferSize;

    private DataReceivedListener mDataReceivedListener;

    @Override
    public synchronized int read(byte[] buffer, int timeout) {
        final Printer logging = mLogging;
        try {
            byte[] bytes = readStream(mInputStream, timeout);
            if (logging != null) {
                logging.println("<<<<< 收：" + ByteUtils.byteArrToHexString(bytes));
            }
            int realLength = bytes.length;
            System.arraycopy(bytes, 0, buffer, 0, bytes.length);
            return realLength;
        } catch (Exception e) {
            LogUtils.e("读取数据异常", e);
            if (e instanceof ProviderTimeoutException) {
                return ErrorCode.ERROR_TIMEOUT;//读取超时
            } else {
                return ErrorCode.ERROR_RECEIVE;//接收数据失败
            }
        }
    }


    @Override
    public synchronized int read(byte[] sendParams, byte[] buffer, int timeout) {
        final Printer logging = mLogging;
        if (logging != null) {
            LogUtils.i(">>>>> 发：" + ByteUtils.byteArrToHexString(sendParams));
        }
        int ret = write(sendParams, timeout);
        if (ret == ErrorCode.ERROR_OK) {
            return read(buffer, timeout);
        } else {
            return ErrorCode.ERROR_SEND;//发送数据失败
        }
    }


    @Override
    public synchronized int write(byte[] sendParams, int timeout) {
        int i = ErrorCode.ERROR_TIMEOUT;
        try {
            if (mOutputStream != null && getState() == ErrorCode.ERROR_OK) {
                mOutputStream.write(sendParams);
                mOutputStream.flush();
                i = ErrorCode.ERROR_OK;
            }
            return i;
        } catch (Exception e) {
            LogUtils.e("write error.", e);
        }
        return i;
    }

    @Override
    public void setReadWaitTimeout(int readWaitTimeout) {
        super.setReadWaitTimeout(readWaitTimeout);
    }

    /**
     * 获取输出流
     *
     * @return
     */
    public OutputStream getOutputStream() {
        return mOutputStream;
    }

    /**
     * 获取输入流
     *
     * @return
     */
    public InputStream getInputStream() {
        return mInputStream;
    }

    /**
     * 启动读取线程
     *
     * @param readType 读取方式，0-{@link #NORMAL}:常规读取(适合写一次数据，数据包一次性返回的场景)，1-{@link #SPLICING}：轮询读取（适合写一次数据，数据包分段返回的场景）
     */
    public void startReadThread(int readType) {
        if (running) {
            return;
        }
        this.readWaitTimeout = 20;
        this.readType = readType;
        mReadThread = new ReadThread();
        mReadThread.start();
    }

    /**
     * 停止读取线程
     */
    public void stopReadThread() {
        running = false;
        if (mReadThread != null) {
            mReadThread.interrupt();
        }
        readBytes = null;
        tempReadBytes = null;
    }

    /**
     * 数据读取线程
     */
    private class ReadThread extends Thread {
        @Override
        public void run() {
            running = true;
            LogUtils.i("Start read thread.");
            InputStream inputStream = getInputStream();
            if (readBufferSize == 0) {
                readBufferSize = 64;
            }
            switch (readType) {
                case NORMAL:
                    normalRead(inputStream, readBufferSize);
                    break;
                case SPLICING:
                    splicingRead(inputStream, readBufferSize);
                    break;
            }


            LogUtils.w("The read thread stop.");
        }
    }

    /**
     * 常规读取,等待inputStream阻塞返回数据
     *
     * @param inputStream
     * @param readBufferSize
     */
    private void normalRead(InputStream inputStream, int readBufferSize) {
        while (running) {
            if (inputStream == null) {
                return;
            }
            byte[] readBuffer = new byte[readBufferSize];

            try {
                int size = inputStream.read(readBuffer);
                if (size > 0) {
                    synchronized (obj) {
                        onDataReceived(readBuffer);
                        notifyRead();
                    }
                }
            } catch (Exception e) {
                LogUtils.e("Data read exception", e);
            }
        }
    }

    /**
     * 轮询读取，判断inputStream中是否还有数据，还有就拼接
     *
     * @param inputStream
     * @param readBufferSize
     */
    private void splicingRead(InputStream inputStream, int readBufferSize) {
        while (running) {
            if (inputStream == null) {
                return;
            }
            byte[] readBuffer = new byte[readBufferSize];
            int size;
            try {
                int len = inputStream.available();
                if (len == 0) {
                    size = 0;
                } else {
                    size = inputStream.read(readBuffer);
                }
                if (size > 0) {
                    if (LogUtils.isDebug()) {
                        LogUtils.i("byte size:" + size + ",data:" + ByteUtils.byteArrToHexString(readBuffer));
                    }
                    readBytes = ByteUtils.arrayAppend(readBytes, readBuffer, size);
                } else {
                    if (readBytes != null) {
                        synchronized (obj) {
                            tempReadBytes = null;
                            onDataReceived(readBytes);
                            notifyRead();
                        }
                    }
                    readBytes = null;
                }
                SystemClock.sleep(readWaitTimeout);
            } catch (Exception e) {
                LogUtils.e("Data read exception", e);
            }
        }
    }


    private void onDataReceived(byte[] readBytes) {
        tempReadBytes = readBytes;
        if (mDataReceivedListener != null) {
            mDataReceivedListener.onDataReceived(readBytes);
        }
    }


    private void waitRead(int timeout) {
        synchronized (obj) {
            try {
                waiter.waitForTimeout(obj, timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void notifyRead() {
        waiter.notifyAll(obj);
    }


    /**
     * 阻塞读取ReadThread的数据，和setDataReceivedListener搭配使用，这样既能监听终端设备的数据（主要指主动上报的数据），又能与设备一发一收通讯，这时候停止使用{@link #read(byte[], int)}和{@link #read(byte[], byte[], int)} (byte[], int)}
     *
     * @param sendParams
     * @param buffer
     * @param timeout
     * @return
     */
    public int readBlocking(byte[] sendParams, byte[] buffer, int timeout) {
        if (getState() != ErrorCode.ERROR_OK) {
            return ErrorCode.ERROR_NOT_CONNECTED;
        }
        int write = write(sendParams, timeout);
        if (write != ErrorCode.ERROR_OK) {
            return write;
        }
        waitRead(timeout);
        if (tempReadBytes != null && tempReadBytes.length > 0) {
            System.arraycopy(tempReadBytes, 0, buffer, 0, tempReadBytes.length);
            return tempReadBytes.length;
        } else {
            return ErrorCode.ERROR_RECEIVE;
        }
    }

    /**
     * 设置数据接收监听器,主要监听设备主动上报的数据(需要客户端解析)，如果需要与设备进行交互，请使用readBlocking方法
     */
    public void setDataReceivedListener(DataReceivedListener dataReceivedListener) {
        this.mDataReceivedListener = dataReceivedListener;
    }

    /**
     * 设置读缓冲区大小
     *
     * @param bufferSize 大小字节数
     */
    public void setReadBufferSize(int bufferSize) {
        this.readBufferSize = bufferSize;
    }

    @Override
    public void close() {
        stopReadThread();
    }
}
