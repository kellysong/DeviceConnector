package com.sjl.deviceconnector.test.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.sjl.deviceconnector.device.serialport.SerialPortHelper;
import com.sjl.deviceconnector.test.adapter.SerialPortListAdapter;
import com.sjl.deviceconnector.test.databinding.BluetoothListActivityBinding;
import com.sjl.deviceconnector.test.databinding.SerialPortListActivityBinding;

import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;


/**
 * 串口列表
 *
 * @author Kelly
 * @version 1.0.0
 * @filename SerialPortListActivity
 * @time 2022/7/25 15:05
 * @copyright(C) 2022 song
 */
public class SerialPortListActivity extends BaseActivity<SerialPortListActivityBinding> {


    @Override
    protected void initView() {

    }

    @Override
    protected void initListener() {

        viewBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void initData() {
        List<String> deviceList = SerialPortHelper.getDeviceList();
        SerialPortListAdapter serialPortListAdapter = new SerialPortListAdapter(deviceList);
        serialPortListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                String item = serialPortListAdapter.getItem(position);
                Intent intent = new Intent();
                intent.putExtra(MainActivity.EXTRA_DEVICE_ITEM, item);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
        viewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        viewBinding.recyclerView.setAdapter(serialPortListAdapter);
    }


}
