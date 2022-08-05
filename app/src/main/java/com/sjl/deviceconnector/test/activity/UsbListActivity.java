package com.sjl.deviceconnector.test.activity;

import android.app.Activity;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.view.View;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.sjl.deviceconnector.device.usb.UsbHelper;
import com.sjl.deviceconnector.listener.UsbPermissionListener;
import com.sjl.deviceconnector.test.adapter.UsbListAdapter;
import com.sjl.deviceconnector.test.databinding.UsbListActivityBinding;

import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;


/**
 * USB列表
 *
 * @author Kelly
 * @version 1.0.0
 * @filename UsbListActivity
 * @time 2022/7/25 12:20
 * @copyright(C) 2022 song
 */
public class UsbListActivity extends BaseActivity<UsbListActivityBinding> {


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
        List<UsbDevice> deviceList = UsbHelper.getDeviceList();
        UsbListAdapter usbListAdapter = new UsbListAdapter(deviceList);
        usbListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                UsbDevice item = usbListAdapter.getItem(position);
                UsbHelper.getInstance().requestPermission(item, new UsbPermissionListener() {
                    @Override
                    public void onGranted(UsbDevice usbDevice) {
                        Intent intent = new Intent();
                        intent.putExtra(MainActivity.EXTRA_DEVICE_ITEM, item);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }

                    @Override
                    public void onDenied(UsbDevice usbDevice) {
                        Toast.makeText(mContext, "Usb设备授权失败：" + usbDevice.getVendorId() + ":" + usbDevice.getProductId(), Toast.LENGTH_LONG).show();
                    }
                });

            }
        });
        viewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        viewBinding.recyclerView.setAdapter(usbListAdapter);

    }

}
