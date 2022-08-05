package com.sjl.deviceconnector.test.activity;

import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.View;

import com.sjl.deviceconnector.test.app.MyApplication;
import com.sjl.deviceconnector.test.databinding.WifiSettingActivityBinding;
import com.sjl.deviceconnector.test.entity.WifiInfo;
import com.sjl.deviceconnector.test.util.SpSettingUtils;

/**
 * Wifi设置
 *
 * @author Kelly
 * @version 1.0.0
 * @filename WifiSettingActivity
 * @time 2022/7/25 15:26
 * @copyright(C) 2022 song
 */
public class WifiSettingActivity extends BaseActivity<WifiSettingActivityBinding> implements TextWatcher {


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
        viewBinding.etIp.addTextChangedListener(this);
        viewBinding.etPort.addTextChangedListener(this);

    }

    @Override
    protected void initData() {
        initDefaultValue();
    }

    private void initDefaultValue() {
        final WifiInfo wifiInfo = MyApplication.getWifiInfo();
        String ip = wifiInfo.getIp();
        int port = wifiInfo.getPort();
        viewBinding.etIp.setText(ip);
        viewBinding.etIp.setInputType(InputType.TYPE_CLASS_NUMBER);
        String digits = "0123456789.";
        viewBinding.etIp.setKeyListener(DigitsKeyListener.getInstance(digits));
        if (!TextUtils.isEmpty(ip)){
            viewBinding.etIp.setSelection(ip.length());
        }
        if (port != 0){
            viewBinding.etPort.setText(String.valueOf(port));
            viewBinding.etPort.setSelection(String.valueOf(port).length());
        }

    }




    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        final WifiInfo wifiInfo = MyApplication.getWifiInfo();
        String ip = viewBinding.etIp.getText().toString().trim();
        if (!TextUtils.isEmpty(ip)) {
            wifiInfo.setIp(ip);
        }
        String port = viewBinding.etPort.getText().toString().trim();
        if (!TextUtils.isEmpty(port)) {
            wifiInfo.setPort(Integer.parseInt(port));
        }
        SpSettingUtils.saveSysWifiInfo(wifiInfo);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
