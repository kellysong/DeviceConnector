package com.sjl.deviceconnector.test.adapter;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.sjl.deviceconnector.entity.AdStructure;
import com.sjl.deviceconnector.test.R;
import com.sjl.deviceconnector.util.ByteUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * ble raw data适配器
 *
 * @author Kelly
 * @version 1.0.0
 * @filename BluetoothLeDataAdapter
 * @time 2023/3/17 21:04
 * @copyright(C) 2023 song
 */
public class BluetoothLeDataAdapter extends BaseQuickAdapter<AdStructure, BaseViewHolder> {
    public BluetoothLeDataAdapter(@Nullable List<AdStructure> data) {
        super(R.layout.ble_data_recycle_item, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, AdStructure item) {

        helper.setText(R.id.tv_len, String.valueOf(item.length))
                .setText(R.id.tv_type, String.format("%02X", item.type & 0xff))
                .setText(R.id.tv_value, ByteUtils.byteArrToHexString(item.data));
    }

}
