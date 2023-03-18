package com.sjl.deviceconnector.test.adapter;

import android.hardware.usb.UsbDevice;
import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.sjl.core.util.log.LogUtils;
import com.sjl.deviceconnector.test.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * TODO
 *
 * @author Kelly
 * @version 1.0.0
 * @filename DeviceListAdapter
 * @time 2022/6/7 12:01
 * @copyright(C) 2022 song
 */
public class UsbListAdapter extends BaseQuickAdapter<UsbDevice, BaseViewHolder> {
    public UsbListAdapter(@Nullable List<UsbDevice> data) {
        super(R.layout.usb_list_recycle_item, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, UsbDevice item) {
        String path = TextUtils.isEmpty(item.getDeviceName()) ? "Unknown" : item.getDeviceName();
        String manufacturerName = "Unknown";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            manufacturerName = TextUtils.isEmpty(item.getManufacturerName()) ? "Unknown" : item.getManufacturerName();
        }
        String productName = "Unknown";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            productName = TextUtils.isEmpty(item.getProductName()) ? "Unknown" : item.getProductName();
        }
        String driver = manufacturerName;


        helper.setText(R.id.tv_driver, driver)
                .setText(R.id.tv_path, path)
                .setText(R.id.tv_name, productName)
                .setText(R.id.tv_manufacturer, manufacturerName)
                .setText(R.id.tv_vendorId, String.valueOf(item.getVendorId()))
                .setText(R.id.tv_productId, String.valueOf(item.getProductId()));
        LogUtils.i("UsbDevice:" + item.toString());
    }
}
