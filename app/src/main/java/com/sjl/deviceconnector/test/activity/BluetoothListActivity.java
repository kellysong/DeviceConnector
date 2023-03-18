package com.sjl.deviceconnector.test.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnItemLongClickListener;
import com.sjl.deviceconnector.device.bluetooth.ble.AdStructureParser;
import com.sjl.deviceconnector.device.bluetooth.scanner.BluetoothClassicScanner;
import com.sjl.deviceconnector.device.bluetooth.BluetoothHelper;
import com.sjl.deviceconnector.device.bluetooth.scanner.BluetoothLowEnergyScanner;
import com.sjl.deviceconnector.entity.AdStructure;
import com.sjl.deviceconnector.entity.BluetoothScanResult;
import com.sjl.deviceconnector.listener.BluetoothScanListener;
import com.sjl.deviceconnector.test.R;
import com.sjl.deviceconnector.test.adapter.BluetoothLeDataAdapter;
import com.sjl.deviceconnector.test.adapter.BluetoothListAdapter;
import com.sjl.deviceconnector.test.databinding.BluetoothListActivityBinding;
import com.sjl.deviceconnector.util.BluetoothUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


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
        viewBinding.swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary));
    }

    @Override
    protected void initListener() {
        viewBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        viewBinding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mBluetoothListAdapter.setNewInstance(null);
                startScan();
            }
        });
    }

    @Override
    protected void initData() {
        boolean scanFlag = getIntent().getBooleanExtra(MainActivity.EXTRA_SCAN_FLAG,false);

        BluetoothAdapter bluetoothAdapter = BluetoothUtils.getBluetoothAdapter();
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
        mBluetoothListAdapter.setScanFlag(scanFlag);
        mBluetoothListAdapter.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                Vibrator vibrator = (Vibrator) BluetoothListActivity.this.getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(60);
                BluetoothScanResult item = (BluetoothScanResult) adapter.getItem(position);
                if (item.getBondState() == BluetoothDevice.BOND_BONDED) {
                    try {
                        boolean result = BluetoothUtils.cancelBond(item.getDevice()); //不一定起适合所有设备，建议前往蓝牙设置操作
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
                BluetoothScanResult item = (BluetoothScanResult) adapter.getItem(position);

                BluetoothHelper.getInstance().stopScan();
                Intent intent = new Intent();
                intent.putExtra(MainActivity.EXTRA_DEVICE_ITEM, item.getDevice());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
        mBluetoothListAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                BluetoothScanResult item = (BluetoothScanResult) adapter.getItem(position);
                switch (view.getId()){
                        case R.id.tv_raw_data:{
                            showBleDialog(item);
                            break;
                        }
                        default:
                            break;
                    }
            }
        });
        viewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        viewBinding.recyclerView.setAdapter(mBluetoothListAdapter);
        if (!scanFlag){
            BluetoothHelper.getInstance().setBluetoothScanner(new BluetoothClassicScanner());
        }else {
            BluetoothHelper.getInstance().setBluetoothScanner(new BluetoothLowEnergyScanner());
        }
        startScan();
    }

    private void showBleDialog(BluetoothScanResult item) {
        View view = LayoutInflater.from(this).inflate(R.layout.ble_data_dialog,null);
        TextView rawData = view.findViewById(R.id.tv_raw_data);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        rawData.setText(AdStructureParser.getHexRawData(item.getScanRecordBytes()));
        List<AdStructure> adStructureList = AdStructureParser.parse(item);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(new BluetoothLeDataAdapter(adStructureList));
        new AlertDialog.Builder(mContext).setNegativeButton("关闭", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setView(view)
                .show();



    }

    private void startScan() {
        BluetoothHelper instance = BluetoothHelper.getInstance();
        instance.stopScan();
        instance.startScan(new BluetoothScanListener() {

            @Override
            public void onDeviceFound(BluetoothScanResult bluetoothScanResult) {
                mBluetoothListAdapter.addNewData(bluetoothScanResult);
            }

            @Override
            public void onScanFinish() {
                if (!isDestroy(BluetoothListActivity.this)){
                    Toast.makeText(mContext, "蓝牙扫描完毕", Toast.LENGTH_SHORT).show();
                    viewBinding.swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }


    /**
     * 初始化已配对设备，减少扫描等待时间
     */
    private void initBondedDevices() {
        List<BluetoothScanResult> bluetoothScanResults = BluetoothUtils.wrapBondedDevices();
        mBluetoothListAdapter.setNewInstance(bluetoothScanResults);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothHelper.getInstance().stopScan();
    }


}
