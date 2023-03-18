package com.sjl.deviceconnector.test.adapter;


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
 * @filename SerialPortListAdapter
 * @time 2022/7/25 14:29
 * @copyright(C) 2022 song
 */
public class SerialPortListAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    public SerialPortListAdapter(@Nullable List<String> data) {
        super(R.layout.serial_port_list_recycle_item, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, String item) {
        helper.setText(R.id.tv_title_name,item);
    }

}
