package com.sjl.deviceconnector.test.adapter;

import android.bluetooth.BluetoothDevice;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.sjl.deviceconnector.entity.BluetoothScanResult;
import com.sjl.deviceconnector.test.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * TODO
 *
 * @author Kelly
 * @version 1.0.0
 * @filename BluetoothListAdapter
 * @time 2022/7/25 10:56
 * @copyright(C) 2022 song
 */
public class BluetoothListAdapter extends BaseQuickAdapter<BluetoothScanResult, BaseViewHolder> {
    private boolean scanFlag;
    public BluetoothListAdapter(@Nullable List<BluetoothScanResult> data) {
        super(R.layout.bluetooth_list_recycle_item, data);
        addChildClickViewIds(R.id.tv_raw_data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, BluetoothScanResult item) {
        String deviceName = item.getName();

        helper.setText(R.id.tv_title_name,deviceName).setText(R.id.tv_address,item.getAddress());
        if (item.getBondState() == BluetoothDevice.BOND_BONDED) {
            helper.setText(R.id.tv_state,"已配对");
        }else {
            helper.setText(R.id.tv_state,"未配对");
        }
        helper.setText(R.id.tv_rssi,String.format("Rssi: %d", item.getRssi()));
        if (!scanFlag){ //经典蓝牙
            helper.setGone(R.id.tv_raw_data,true);
        }else {
            helper.setGone(R.id.tv_raw_data,false);
        }
    }

    public void addNewData(BluetoothScanResult device) {
        List<BluetoothScanResult> datas = getData();
        if (datas == null || datas.size() == 0){
            addData(device);
            return;
        }
        boolean exist = false;
        for (BluetoothScanResult bluetoothDevice:datas){
            if (bluetoothDevice.getAddress().equals(device.getAddress())){
                exist = true;
            }
        }
        if (!exist){
            addData(device);
        }
        //按信号强弱从大到小排序
        Collections.sort(getData(), new Comparator<BluetoothScanResult>() {
            @Override
            public int compare(BluetoothScanResult o1, BluetoothScanResult o2) {
                return o2.getRssi()- o1.getRssi();
            }
        });
        notifyDataSetChanged();

    }

    public void setScanFlag(boolean scanFlag) {
        this.scanFlag = scanFlag;
    }
}
