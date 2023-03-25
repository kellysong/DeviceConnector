package com.sjl.deviceconnector.provider;

import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import com.hoho.android.usbserial.util.MonotonicClock;
import com.sjl.deviceconnector.DeviceContext;
import com.sjl.deviceconnector.ErrorCode;
import com.sjl.deviceconnector.device.usb.UsbHelper;
import com.sjl.deviceconnector.exception.ProviderTimeoutException;
import com.sjl.deviceconnector.util.LogUtils;

import java.io.IOException;

/**
 * USB连接提供者(基于Android USB封装,批量传输 bulkTransfer,不是控制传输)
 * <p>由于不同设备协议不同和使用传输方式不同，如果发送的数据，无法正常返回想要结果，建议继承此类重写{@link UsbConnectProvider#write(byte[], int)}和{@link UsbConnectProvider#read(byte[], byte[], int)}</p>
 * <p>最后，按照设备协议组装数据及进行读写操作</p>
 *
 * @author Kelly
 * @version 1.0.0
 * @filename UsbConnectProvider.java
 * @time 2018/4/13 8:46
 * @copyright(C) 2018 song
 */
public class UsbConnectProvider extends BaseConnectProvider {

    /**
     * USB设备
     */
    private UsbDevice mUsbDevice;
    /**
     * USB接口
     */
    private UsbInterface mUsbInterface;
    /**
     * USB输出端点,USB HID主要就是通过节点进行通信的
     */
    protected UsbEndpoint mUsbEndpointOut;
    /**
     * USB输入端点,USB HID主要就是通过节点进行通信的
     */
    protected UsbEndpoint mUsbEndpointIn;
    /**
     * USB连接
     */
    protected UsbDeviceConnection mDeviceConnection;
    private int maxWriteBuffer;
    private int maxReadBuffer;
    protected byte[] mWriteBuffer;
    private static final int DEFAULT_WRITE_BUFFER_SIZE = 64 ;
    private static final int MAX_READ_SIZE = 16 * 1024; // = old bulkTransfer limit

    /**
     * 初始化Usb连接提供
     *
     * @param vendorId  产商id
     * @param productId 产品id
     */
    public UsbConnectProvider(int vendorId, int productId) {
        this(UsbHelper.getDevice(vendorId, productId));
    }

    /**
     * 初始化Usb连接提供
     */
    public UsbConnectProvider(UsbDevice usbDevice) {
        this.mUsbDevice = usbDevice;
        mWriteBuffer = new byte[DEFAULT_WRITE_BUFFER_SIZE];
    }


    @Override
    public int open() {
        int state = getState();
        if (state == ErrorCode.ERROR_OK) {
            return state;
        }
        if (mUsbDevice == null) {
            LogUtils.e("connection failed: device not found");
            return ErrorCode.ERROR_DEVICE_NOT_FIND;
        }
        int ret = getDeviceInterface();
        if (ret == ErrorCode.ERROR_OK) {
            boolean openFlag = openDevice();//进行连接
            if (openFlag) {
                return ErrorCode.ERROR_OK;
            } else {
                return ErrorCode.ERROR_OPEN_FAIL;
            }
        }
        return ret;
    }


    /**
     * 获取设备接口
     *
     * @return
     */
    private int getDeviceInterface() {
        int interfaceCount = mUsbDevice.getInterfaceCount();
        LogUtils.i("interfaceCounts : " + interfaceCount);
        if (interfaceCount > 0) {
            mUsbInterface = mUsbDevice.getInterface(0);//获取设备接口，一般都是一个接口
            LogUtils.i("成功获得设备接口:" + mUsbInterface.getId());
            return ErrorCode.ERROR_OK;
        } else {
            LogUtils.w("没有找到设备接口");
            return ErrorCode.ERROR_FAIL;
        }
    }


    /**
     * 连接设备
     *
     * @return
     */
    private boolean openDevice() {
        UsbDeviceConnection connection;
        UsbManager usbManager = (UsbManager) DeviceContext.getContext().getSystemService(Context.USB_SERVICE);
        // 判断是否有权限
        if (usbManager.hasPermission(mUsbDevice)) {
            // 打开设备，获取 UsbDeviceConnection 对象，连接设备，用于后面的通讯
            connection = usbManager.openDevice(mUsbDevice);
            if (connection == null) {
                LogUtils.w("不能连接到设备");
                return false;
            }
            //打开设备
            if (connection.claimInterface(mUsbInterface, true)) {
                LogUtils.i("打开设备成功");
                mDeviceConnection = connection;
                //UsbInterface 进行端点设置和通讯
                assignEndpoint(mUsbInterface);
                this.mConnectState = true;
                return true;
            } else {
                LogUtils.w("无法打开连接通道");
                connection.close();
                return false;
            }
        } else {
            LogUtils.w("没有权限");
            //触发一次权限申请，正常使用需要在外部申请usb权限
            UsbHelper.getInstance().requestPermission(mUsbDevice);
            return false;
        }
    }

    /**
     * 分配端点，IN | OUT，即输入输出
     */
    private void assignEndpoint(UsbInterface usbInterface) {
        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
            UsbEndpoint ep = usbInterface.getEndpoint(i);
            LogUtils.i("UsbEndpoint类型：" + ep.getType());
            switch (ep.getType()) {
                case UsbConstants.USB_ENDPOINT_XFER_BULK:// 批量传输
                    if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {//输出
                        mUsbEndpointOut = ep;
                        maxWriteBuffer = mUsbEndpointOut.getMaxPacketSize();
                        LogUtils.i("找到BulkEndpointOut," + "index:" + i + ",使用端点号：" + mUsbEndpointOut.getEndpointNumber() + ",mUsbEndpointOut:" + mUsbEndpointOut);
                    }

                    if (ep.getDirection() == UsbConstants.USB_DIR_IN) {
                        mUsbEndpointIn = ep;
                        maxReadBuffer = mUsbEndpointIn.getMaxPacketSize();
                        LogUtils.i("找到BulkEndpointIn:" + "index:" + i + ",使用端点号：" + mUsbEndpointIn.getEndpointNumber() + ",mUsbEndpointIn:" + mUsbEndpointIn);
                    }
                    break;
                case UsbConstants.USB_ENDPOINT_XFER_INT:// 中断传输
                    if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {//输出
                        mUsbEndpointOut = ep;
                        maxWriteBuffer = mUsbEndpointOut.getMaxPacketSize();
                        LogUtils.i("找到InterruptEndpointOut:" + "index:" + i + ",使用端点号：" + mUsbEndpointOut.getEndpointNumber() + ",mUsbEndpointOut:" + mUsbEndpointOut);
                    }
                    if (ep.getDirection() == UsbConstants.USB_DIR_IN) {
                        mUsbEndpointIn = ep;
                        maxReadBuffer = mUsbEndpointIn.getMaxPacketSize();
                        LogUtils.i("找到InterruptEndpointIn:" + "index:" + i + ",使用端点号：" + mUsbEndpointIn.getEndpointNumber() + ",mUsbEndpointIn:" + mUsbEndpointIn);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public synchronized int write(byte[] sendParams, int timeout) {
        int state = getState();
        if (state != ErrorCode.ERROR_OK) {
            return state;
        }
        int offset = 0;
        final long endTime = (timeout == 0) ? 0 : (MonotonicClock.millis() + timeout);

        if (mWriteBuffer == null){
            mWriteBuffer = new byte[Math.min(DEFAULT_WRITE_BUFFER_SIZE, mWriteBuffer.length)];
        }
        while (offset < sendParams.length) {
            int requestTimeout;
            final int requestLength;
            final int actualLength;
            final byte[] writeBuffer;
            requestLength = Math.min(sendParams.length - offset, mWriteBuffer.length);
            if (offset == 0) {
                writeBuffer = sendParams;
            } else {
                // bulkTransfer does not support offsets, make a copy.
                System.arraycopy(sendParams, offset, mWriteBuffer, 0, requestLength);
                writeBuffer = mWriteBuffer;
            }
            if (timeout == 0 || offset == 0) {
                requestTimeout = timeout;
            } else {
                requestTimeout = (int) (endTime - MonotonicClock.millis());
                if (requestTimeout == 0)
                    requestTimeout = -1;
            }
            if (requestTimeout < 0) {
                actualLength = -2;
            } else {
                actualLength = mDeviceConnection.bulkTransfer(mUsbEndpointOut, writeBuffer, requestLength, requestTimeout);
            }
            if (LogUtils.isDebug()){
                LogUtils.i("Wrote " + actualLength + "/" + requestLength + " offset " + offset + "/" + sendParams.length + " timeout " + requestTimeout);
            }
            if (actualLength <= 0) {
                if (timeout != 0 && MonotonicClock.millis() >= endTime) {
                    ProviderTimeoutException ex = new ProviderTimeoutException("Error writing " + requestLength + " bytes at offset " + offset + " of total " + sendParams.length + ", rc=" + actualLength);
                    throw ex;
                } else {
                    throw new RuntimeException("Error writing " + requestLength + " bytes at offset " + offset + " of total " + sendParams.length);
                }
            }
            offset += actualLength;
        }
        return ErrorCode.ERROR_OK;
    }

    @Override
    public synchronized int read(byte[] buffer, int timeout) {
        int state = getState();
        if (state != ErrorCode.ERROR_OK) {
            return state;
        }
        final int readMax = Math.min(buffer.length, MAX_READ_SIZE);
        final int read = mDeviceConnection.bulkTransfer(mUsbEndpointIn, buffer, readMax, timeout);
        return read;
    }


    @Override
    public synchronized int read(byte[] sendParams, byte[] buffer, int timeout) {
        int result = write(sendParams, timeout);
        if (result == ErrorCode.ERROR_OK) {
            return read(buffer, timeout);
        } else {
            LogUtils.i("发送收数据失败");
            return ErrorCode.ERROR_SEND;//发送收数据失败
        }

    }


    @Override
    public void close() {
        mConnectState = false;
        if (mDeviceConnection != null) {
            mDeviceConnection.releaseInterface(mUsbInterface);
            mDeviceConnection.close();
        }
        mUsbEndpointIn = null;
        mUsbEndpointOut = null;
    }

    /**
     * 设置发送缓冲区大小
     *
     * @param bufferSize 大小字节数
     */
    public void setWriteBufferSize(int bufferSize) {
        mWriteBuffer = new byte[bufferSize];
    }

    public int getMaxWriteBuffer() {
        return maxWriteBuffer;
    }

    public int getMaxReadBuffer() {
        return maxReadBuffer;
    }
}
