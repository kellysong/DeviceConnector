package com.sjl.deviceconnector.provider;


import android.util.Printer;

import com.sjl.deviceconnector.ErrorCode;
import com.sjl.deviceconnector.exception.ProviderTimeoutException;
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
    protected InputStream mInputStream;
    protected OutputStream mOutputStream;


    @Override
    public synchronized int read(byte[] sendParams, byte[] buffer, int timeout) {
        final Printer logging = mLogging;
        if (logging != null) {
            LogUtils.i(">>>>> 发：" + ByteUtils.byteArrToHexString(sendParams));
        }
        int ret = write(sendParams, timeout);
        if (ret == ErrorCode.ERROR_OK) {
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
}
