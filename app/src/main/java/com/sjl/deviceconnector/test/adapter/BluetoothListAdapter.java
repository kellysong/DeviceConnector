package com.sjl.deviceconnector.test.adapter;

import android.bluetooth.BluetoothDevice;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.sjl.deviceconnector.test.R;

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
public class BluetoothListAdapter extends BaseQuickAdapter<BluetoothDevice, BaseViewHolder> {
    public BluetoothListAdapter(@Nullable List<BluetoothDevice> data) {
        super(R.layout.bluetooth_list_recyle_item, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, BluetoothDevice item) {
        String deviceName;
        if (item.getName() == null || "null".equals(item.getName())) {
            deviceName = "Unknown";
        } else {
            deviceName = item.getName();
        }
        helper.setText(R.id.tv_title_name,deviceName).setText(R.id.tv_address,item.getAddress());
        if (item.getBondState() == BluetoothDevice.BOND_BONDED) {
            helper.setText(R.id.tv_state,"已配对");
        }else {
            helper.setText(R.id.tv_state,"未配对");
        }
    }

    public void addNewData(BluetoothDevice device) {
        List<BluetoothDevice> datas = getData();
        if (datas == null || datas.size() == 0){
            addData(device);
            return;
        }
        for (BluetoothDevice bluetoothDevice:datas){
            if (!bluetoothDevice.getAddress().equals(device.getAddress())){
                addData(device);
                break;
            }
        }
    }
}
