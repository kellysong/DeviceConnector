package com.sjl.deviceconnector.test.activity;

import android.view.View;
import android.widget.TextView;

import com.sjl.deviceconnector.test.app.MyApplication;
import com.sjl.deviceconnector.test.R;
import com.sjl.deviceconnector.test.databinding.SerialPortSettingActivityBinding;
import com.sjl.deviceconnector.test.entity.SerialPortInfo;
import com.sjl.deviceconnector.test.util.SpSettingUtils;

import androidx.appcompat.app.AlertDialog;

/**
 * 串口，保存到内存中
 *
 * @author Kelly
 * @version 1.0.0
 * @filename SerialPortSettingActivity
 * @time 2022/6/9 18:00
 * @copyright(C) 2022 song
 */
public class SerialPortSettingActivity extends BaseActivity<SerialPortSettingActivityBinding> implements View.OnClickListener {
    TextView tvBaudRate;
    TextView tvDataBits;
    TextView tvStopBits;
    TextView tvParity;

    private int baudRate = 115200;
    private int dataBits = 8;
    private int stopBits = 1;
    private int parity = 0;

    @Override
    protected void initView() {
        tvBaudRate = viewBinding.tvBaudRate;
        tvDataBits = viewBinding.tvDataBits;
        tvStopBits = viewBinding.tvStopBits;
        tvParity = viewBinding.tvParity;
    }

    @Override
    protected void initListener() {
        viewBinding.itemBaudRate.setOnClickListener(this);
        viewBinding.itemDataBits.setOnClickListener(this);
        viewBinding.itemStopBits.setOnClickListener(this);
        viewBinding.itemParity.setOnClickListener(this);
        viewBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void initData() {
        initDefaultValue();
    }

    private void initDefaultValue() {
        final SerialPortInfo serialPortInfo = MyApplication.getSerialPortInfo();
        tvBaudRate.setText(String.valueOf(serialPortInfo.getBaudRate()));
        tvDataBits.setText(String.valueOf(serialPortInfo.getDataBits()));
        if (serialPortInfo.getStopBits() == 3) {
            tvStopBits.setText("1.5");
        } else {
            tvStopBits.setText(String.valueOf(serialPortInfo.getStopBits()));
        }
        int parity = serialPortInfo.getParity();
        final String[] values = getResources().getStringArray(R.array.parity);
        tvParity.setText(values[parity]);


    }



    @Override
    public void onClick(View v) {
        final SerialPortInfo serialPortInfo = MyApplication.getSerialPortInfo();

        switch (v.getId()) {

            case R.id.itemBaudRate: {
                final String[] values = getResources().getStringArray(R.array.baudRates);
                int pos = java.util.Arrays.asList(values).indexOf(String.valueOf(baudRate));
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("波特率");
                builder.setSingleChoiceItems(values, pos, (dialog, which) -> {
                    dialog.dismiss();
                    baudRate = Integer.parseInt(values[which]);
                    tvBaudRate.setText(values[which]);
                    serialPortInfo.setBaudRate(baudRate);
                    saveSerialPortInfo(serialPortInfo);
                });
                builder.create().show();
                break;
            }
            case R.id.itemDataBits: {
                final String[] values = getResources().getStringArray(R.array.dataBits);
                int pos = java.util.Arrays.asList(values).indexOf(String.valueOf(dataBits));
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("数据位");
                builder.setSingleChoiceItems(values, pos, (dialog, which) -> {
                    dialog.dismiss();
                    dataBits = Integer.parseInt(values[which]);
                    tvDataBits.setText(values[which]);
                    serialPortInfo.setDataBits(dataBits);
                    saveSerialPortInfo(serialPortInfo);
                });
                builder.create().show();
                break;
            }
            case R.id.itemStopBits: {
                final String[] values = getResources().getStringArray(R.array.stopBits);
                int pos = 0;
                if (stopBits == 1) {
                    pos = 0;
                } else if (stopBits == 3) {
                    pos = 1;
                } else if (stopBits == 2) {
                    pos = 2;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("停止位");
                builder.setSingleChoiceItems(values, pos, (dialog, which) -> {
                    dialog.dismiss();
                    if (values[which].equals("1.5")) {
                        stopBits = 3;
                    } else {
                        stopBits = Integer.parseInt(values[which]);
                    }
                    tvStopBits.setText(values[which]);

                    serialPortInfo.setStopBits(stopBits);
                    saveSerialPortInfo(serialPortInfo);
                });
                builder.create().show();
                break;
            }
            case R.id.itemParity: {
                final String[] values = getResources().getStringArray(R.array.parity);

                int pos = parity;
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("校验位");
                builder.setSingleChoiceItems(values, pos, (dialog, which) -> {
                    dialog.dismiss();
                    parity = which;
                    tvParity.setText(values[which]);
                    serialPortInfo.setParity(which);
                    saveSerialPortInfo(serialPortInfo);
                });
                builder.create().show();
                break;
            }
            default:
                break;
        }

    }

    private void saveSerialPortInfo(SerialPortInfo serialPortInfo) {
        SpSettingUtils.saveSerialPortInfo(serialPortInfo);
    }

}
