package com.sjl.deviceconnector.device.bluetooth.ble.request;

/**
 * mtu设置请求
 *
 * @author Kelly
 * @version 1.0.0
 * @filename MtuRequest
 * @time 2023/3/4 14:33
 * @copyright(C) 2023 song
 */
public class MtuRequest extends BluetoothLeRequest {
    private int mtu;

    public int getMtu() {
        return mtu;
    }

    /**
     *
     * 设置MTU，不同的蓝牙版本最大MTU不同，蓝牙4.2的最大MTU=247Byte，蓝牙5.0的最大MTU=512Byte。
     * <p>蓝牙一般默认支持的MTU长度是23个字节，一有效的最大MTU还需要减去协议Byte、Opcode和Handler，实际传输数据就是20字节。</p>
     *
     * <p>通过gatt.requestMtu(mtu)。会触发onMtuChanged回调。这里mtu 的范围在23 ~ 512之间，目前市面上Android版本高的手机（支持蓝牙5.0）基本上都是247。也就是设置mtu = 512，回调中的mtu可能还是247,真本事因为手机做了限制</p>
     *
     *
     * @param mtu 23字节~512字节
     */
    public void setMtu(int mtu) {
        this.mtu = mtu;
    }
}
