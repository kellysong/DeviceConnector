package com.sjl.deviceconnector.device.bluetooth.ble;

import com.sjl.deviceconnector.entity.AdStructure;
import com.sjl.deviceconnector.entity.BluetoothScanResult;
import com.sjl.deviceconnector.util.ByteUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * AdStructure解析器
 *
 * @author Kelly
 * @version 1.0.0
 * @filename AdStructureParser
 * @time 2023/3/17 21:13
 * @copyright(C) 2023 song
 */
public class AdStructureParser {

    /**
     * 获取原始数据（16进制字符串）
     * @param scanRecordBytes
     * @return
     */
    public static String getHexRawData(byte[] scanRecordBytes) {
        return ByteUtils.byteArrToHexString(getRawData(scanRecordBytes));
    }

    /**
     * 获取原始数据（字节数组）
     * @param scanRecordBytes
     * @return
     */
    public static byte[] getRawData(byte[] scanRecordBytes) {
        int to = 0;
        for (int i = scanRecordBytes.length - 1 ; i >= 0; i--) {
            if (scanRecordBytes[i] != 0) {
                to = i;
                break;
            }
        }
        return Arrays.copyOfRange(scanRecordBytes, 0, to + 1);
    }
    
    /**
     * 根据AdStructure规则解析
     *
     * @param item
     * @return
     * @see AdStructure
     */
    public static List<AdStructure> parse(BluetoothScanResult item) {
        return parse(item.getScanRecordBytes());
    }

    /**
     * 根据AdStructure规则解析
     *
     * @param scanRecordBytes
     * @return
     * @see AdStructure
     */
    public static List<AdStructure> parse(byte[] scanRecordBytes) {
        if (scanRecordBytes == null) {
            return Collections.EMPTY_LIST;
        }
        List<AdStructure> adStructureList = new ArrayList<AdStructure>();
        AdStructure adStructure;
        for (int i = 0; i < scanRecordBytes.length; ) {
            if (scanRecordBytes.length - i >= 2) {

                byte length = scanRecordBytes[i];
                int from = i + 2;
                if (length > 0 && from < scanRecordBytes.length) {
                    adStructure = new AdStructure();
                    byte type = scanRecordBytes[i + 1];
                    int endIndex = from + length - 1;
                    if (endIndex >= scanRecordBytes.length) {
                        endIndex = scanRecordBytes.length - 1;
                    }
                    adStructure.length = length;
                    adStructure.type = type;
                    byte[] data = Arrays.copyOfRange(scanRecordBytes, from, endIndex);
                    adStructure.data = data;
                    i += length + 1;
                    adStructureList.add(adStructure);
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        return adStructureList;
    }


}
