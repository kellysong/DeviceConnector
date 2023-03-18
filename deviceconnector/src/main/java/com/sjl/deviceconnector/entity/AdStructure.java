package com.sjl.deviceconnector.entity;

import com.sjl.deviceconnector.util.ByteUtils;

/**
 * 广播数据单元
 *
 * <p> 广播包中包含若干个广播数据单元，广播数据单元也称为 AD Structure。</p>
 * <p> 广播数据单元 = 长度值Length + AD type + AD Data。</p>
 * <p> 长度值Length只占一个字节，并且位于广播数据单元的第一个字节;AD type也是一个字节;AD Data长度为Length-1</p>
 *
 * @author Kelly
 * @version 1.0.0
 * @filename AdStructure
 * @time 2023/3/12 20:28
 * @copyright(C) 2023 song
 */
public class AdStructure {
    /**
     * 广播中声明的长度
     */
    public int length;

    /**
     * 广播中声明的type
     * <p>type = 0x01 表示设备LE物理连接。</p>
     * <p>type = 0x09 表示设备的全名（转为ASCII码）</p>>
     * <p>type = 0x03 表示完整的16bit UUID。</p>
     * <p>type = 0xFF 表示厂商数据。前两个字节表示厂商ID,后面的为厂商数据，具体由用户自行定义。</p>
     * <p> type = 0x16 结合Type = 0x03看，表示16 bit UUID对应的数据，具体由用户自行定义。</p>
     *
     */
    public int type;

    /**
     * 广播中的数据部分
     */
    public byte[] data;


    @Override
    public String toString() {
        return "AdStructure{" +
                "length=0x" +String.format("%02X", length & 0xff)  +"->"+length+
                ", type=0x" + String.format("%02X", type & 0xff)  +"->"+type+
                ", data=0x" + ByteUtils.byteArrToHexString(data) +
                '}';
    }
}
