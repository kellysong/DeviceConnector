package com.sjl.deviceconnector.provider;


import android.util.Printer;

import com.sjl.deviceconnector.ErrorCode;
import com.sjl.deviceconnector.util.LogUtils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.InputStream;
import java.net.SocketTimeoutException;

/**
 * 通用方法基类
 *
 * @author Kelly
 */
public abstract class BaseConnectProvider implements IConnectProvider {

    /**
     * 报文打印接口
     */
    protected Printer mLogging;
    /**
     * 连接状态
     */
    protected boolean mConnectState = false;
    /**
     * 每次读取等待超时时间，防止漏读,单位：毫秒
     */
    protected int readWaitTimeout = 100;

    @Override
    public int getState() {
        return mConnectState ? ErrorCode.ERROR_OK : ErrorCode.ERROR_NOT_CONNECTED;
    }

    /**
     * 重载多一个,适合发送不需处理返回值的命令
     *
     * @param sendParams
     * @return
     */
    public int write(byte[] sendParams) {
        return write(sendParams, 0);
    }


    /**
     * 读取字节
     *
     * @param inStream
     * @param timeout
     * @return
     * @throws Exception
     */
    protected byte[] readStream(InputStream inStream, int timeout) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        readStreamWithRecursion(output, inStream, timeout);
        output.close();
        return output.toByteArray();
    }


    /**
     * 读取字符串
     *
     * @param inStream
     * @return
     * @throws Exception
     */
    protected String readString(InputStream inStream, int timeout) throws Exception {
        return new String(readStream(inStream, timeout));
    }


    /**
     * 递归读取流
     *
     * @param output
     * @param inStream
     * @param timeout  单位毫秒
     * @return
     * @throws Exception
     */
    private void readStreamWithRecursion(ByteArrayOutputStream output, InputStream inStream, int timeout) throws Exception {
        long start = System.currentTimeMillis();
        while (inStream.available() == 0) {
            if ((System.currentTimeMillis() - start) > timeout) {//超时退出
                LogUtils.w("超时读取.....");
                throw new SocketTimeoutException("超时读取");
            }
        }
        byte[] buffer = new byte[128];
        int read = inStream.read(buffer);
        output.write(buffer, 0, read);
        final int wait = readWaitTimeout;
        //二次读取等待，防止漏读
        long startWait = System.currentTimeMillis();
        boolean checkExist = false;
        while (System.currentTimeMillis() - startWait <= wait) {
            int a = inStream.available();
            if (a > 0) {
                checkExist = true;
                break;
            }

        }
        if (checkExist) {
            if (!checkMessage(buffer, read)) {
                readStreamWithRecursion(output, inStream, timeout);
            }
        }

    }


    /**
     * 验证读取消息
     *
     * @param buffer
     * @param read
     * @return true验证，false不验证
     */
    private boolean checkMessage(byte[] buffer, int read) {
        if (read <= buffer.length) {
            return checkRule(buffer, read);
        }
        return false;
    }

    /**
     * 报文验证规则，需要验证子类覆写
     *
     * @param buffer
     * @param read
     * @return
     */
    protected boolean checkRule(byte[] buffer, int read) {
        return false;
    }


    public void setReadWaitTimeout(int readWaitTimeout) {
        this.readWaitTimeout = readWaitTimeout;
    }


    /**
     * 关闭流
     *
     * @param x
     */
    public static void close(Closeable x) {
        if (x != null) {
            try {
                x.close();
            } catch (Exception e) {
                // skip
            }
        }
    }


    /**
     * 通讯报文打印
     *
     * @param printer
     */
    public void setCmdLogging(Printer printer) {
        mLogging = printer;
    }

}
