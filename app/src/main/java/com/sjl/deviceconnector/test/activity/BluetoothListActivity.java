package com.sjl.deviceconnector.test.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Vibrator;
import android.view.View;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnItemLongClickListener;
import com.sjl.deviceconnector.device.bluetooth.BluetoothHelper;
import com.sjl.deviceconnector.listener.BluetoothScanListener;
import com.sjl.deviceconnector.test.adapter.BluetoothListAdapter;
import com.sjl.deviceconnector.test.databinding.BluetoothListActivityBinding;

import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;


/**
 * 蓝牙列表
 *
 * @author Kelly
 * @version 1.0.0
 * @filename BluetoothListActivity
 * @time 2022/7/25 10:52
 * @copyright(C) 2022 song
 */
public class BluetoothListActivity extends BaseActivity<BluetoothListActivityBinding> {

    private BluetoothListAdapter mBluetoothListAdapter;

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
        BluetoothAdapter bluetoothAdapter = BluetoothHelper.getBluetoothAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "该设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        boolean enabled = bluetoothAdapter.isEnabled();
        if (!enabled) {
            Toast.makeText(this, "请先打开蓝牙", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        mBluetoothListAdapter = new BluetoothListAdapter(null);
        mBluetoothListAdapter.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                Vibrator vibrator = (Vibrator) BluetoothListActivity.this.getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(60);
                BluetoothDevice item = (BluetoothDevice) adapter.getItem(position);
                if (item.getBondState() == BluetoothDevice.BOND_BONDED) {
                    try {
                        boolean result = BluetoothHelper.cancelBond(item); //不一定起适合所有设备，建议前往蓝牙设置操作
                        if (result) {
                            adapter.remove(position);
                        } else {
                            Toast.makeText(BluetoothListActivity.this, "取消已配对设备失败", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(BluetoothListActivity.this, "无法取消未配对设备", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });


        mBluetoothListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                BluetoothDevice item = (BluetoothDevice) adapter.getItem(position);
                BluetoothHelper.getInstance().stopScan();
                Intent intent = new Intent();
                intent.putExtra(MainActivity.EXTRA_DEVICE_ITEM, item);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
        viewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        viewBinding.recyclerView.setAdapter(mBluetoothListAdapter);
        initBondedDevices();


        BluetoothHelper.getInstance().startScan(new BluetoothScanListener() {
            @Override
            public void onDeviceFound(BluetoothDevice bluetoothDevice) {
                mBluetoothListAdapter.addNewData(bluetoothDevice);
            }

            @Override
            public void onScanFinish() {
                Toast.makeText(mContext, "蓝牙扫描完毕", Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * 初始化已配对设备，减少扫描等待时间
     */
    private void initBondedDevices() {
        List<BluetoothDevice> bondedDevices = BluetoothHelper.getBondedDevices();
        mBluetoothListAdapter.setNewData(bondedDevices);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothHelper.getInstance().stopScan();
    }


}
