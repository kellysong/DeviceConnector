package com.sjl.deviceconnector.test.activity;


import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;


import com.sjl.core.permission.PermissionsManager;
import com.sjl.core.permission.PermissionsResultAction;
import com.sjl.core.permission.SpecialPermission;
import com.sjl.core.util.log.LogUtils;
import com.sjl.deviceconnector.ErrorCode;
import com.sjl.deviceconnector.device.bluetooth.BluetoothHelper;
import com.sjl.deviceconnector.device.usb.UsbHelper;
import com.sjl.deviceconnector.listener.ConnectedListener;
import com.sjl.deviceconnector.listener.UsbPlugListener;
import com.sjl.deviceconnector.provider.BaseConnectProvider;
import com.sjl.deviceconnector.provider.BluetoothConnectProvider;
import com.sjl.deviceconnector.provider.SerialPortConnectProvider;
import com.sjl.deviceconnector.provider.UsbComConnectProvider;
import com.sjl.deviceconnector.provider.UsbConnectProvider;
import com.sjl.deviceconnector.provider.WifiConnectProvider;
import com.sjl.deviceconnector.test.app.MyApplication;
import com.sjl.deviceconnector.test.R;
import com.sjl.deviceconnector.test.databinding.MainActivityBinding;
import com.sjl.deviceconnector.test.entity.ConnectWay;
import com.sjl.deviceconnector.test.entity.MessageEvent;
import com.sjl.deviceconnector.test.entity.SerialPortInfo;
import com.sjl.deviceconnector.test.entity.WifiInfo;
import com.sjl.deviceconnector.test.service.BluetoothService;
import com.sjl.deviceconnector.test.service.WifiService;
import com.sjl.deviceconnector.util.ByteUtils;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends BaseActivity<MainActivityBinding> implements View.OnClickListener {
    private static final int REQUEST_CONNECT_DEVICE = 100;

    public static String EXTRA_DEVICE_ITEM = "device_item";

    private ConnectWay connectWay = ConnectWay.SERIAL_PORT;
    private BaseConnectProvider baseConnectProvider;
    private String devicePath;
    private UsbDevice usbDevice;
    private BluetoothDevice bluetoothDevice;
    private String[] permissions = new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION};
    private Intent bluetoothServiceIntent,wifiServiceIntent;
    /**
     * ???????????????true 16?????????false????????????
     */
    boolean sendFormat = false;
    /**
     * ???????????????true 16?????????false????????????
     */
    boolean receiveFormat = false;
    @Override
    protected void initView() {
        viewBinding.sendText.setInputType(InputType.TYPE_CLASS_TEXT);
        String digits = "0123456789abcdefABCDEF";
        viewBinding.sendText.setKeyListener(DigitsKeyListener.getInstance(digits));
    }

    @Override
    protected void initListener() {
        viewBinding.btnOpenSerialPortList.setOnClickListener(this);
        viewBinding.btnOpenSerialPortConfig.setOnClickListener(this);
        viewBinding.btnOpenUsbSerialPortList.setOnClickListener(this);
        viewBinding.btnOpenUsbSerialPortConfig.setOnClickListener(this);
        viewBinding.btnOpenUsbList.setOnClickListener(this);
        viewBinding.btnOpenBluetoothList.setOnClickListener(this);
        viewBinding.btnOpenWifiSetting.setOnClickListener(this);

        viewBinding.btnConnect.setOnClickListener(this);
        viewBinding.btnDisconnect.setOnClickListener(this);
        viewBinding.btnSend.setOnClickListener(this);
        viewBinding.btnClear.setOnClickListener(this);
        viewBinding.btnTestServer.setOnClickListener(this);
        UsbHelper.getInstance().setUsbPlugListener(new UsbPlugListener() {
            @Override
            public void onAttached(UsbDevice usbDevice) {

            }

            @Override
            public void onDetached(UsbDevice usbDevice) {

            }
        });
        UsbHelper.getInstance().setConnectedListener(new ConnectedListener<UsbDevice>() {
            @Override
            public void onResult(UsbDevice device, boolean connected) {

            }
        });
        UsbHelper.getInstance().registerReceiver();

        //?????????baseConnectProvider.close();?????????????????????
        BluetoothHelper.getInstance().setConnectedListener(new ConnectedListener<BluetoothDevice>() {
            @Override
            public void onResult(BluetoothDevice device, boolean connected) {
                if (!connected){
                    Toast.makeText(MainActivity.this, "????????????" + device.getName() + "???????????????", Toast.LENGTH_LONG).show();
                }
            }
        });
        BluetoothHelper.getInstance().registerReceiver();


    }

    @Override
    protected void initData() {
        ConnectWay[] connectWays = ConnectWay.values();
        viewBinding.spinnerConnectWay.setAdapter(new ArrayAdapter<ConnectWay>(this, android.R.layout.simple_list_item_1, connectWays));
        viewBinding.spinnerConnectWay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    Class<?> clazz = AdapterView.class;
                    Field field = clazz.getDeclaredField("mOldSelectedPosition");
                    field.setAccessible(true);
                    field.setInt(viewBinding.spinnerConnectWay,AdapterView.INVALID_POSITION);
                } catch(Exception e){
                    e.printStackTrace();
                }
                return false;
            }
        });
        viewBinding.spinnerConnectWay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                connectWay = connectWays[position];
                initDeviceList(connectWay);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(this, permissions, new PermissionsResultAction() {
            @Override
            public void onGranted() {
                LogUtils.i("?????????????????????????????????");
                initDataWithPermission();
            }

            @Override
            public void onDenied(String permission) {
                LogUtils.i("???????????????" + permission);
                finish();
            }
        });

    }

    private void initDataWithPermission() {
    }


    private void initDeviceList(ConnectWay connectWay) {
        disconnect(null);
        switch (connectWay) {
            case SERIAL_PORT: {
                viewBinding.btnTestServer.setVisibility(View.GONE);
                viewBinding.llSerialPortList.setVisibility(View.VISIBLE);
                viewBinding.llUsbSerialPortList.setVisibility(View.GONE);
                viewBinding.btnOpenUsbList.setVisibility(View.GONE);
                viewBinding.btnOpenBluetoothList.setVisibility(View.GONE);
                viewBinding.btnOpenWifiSetting.setVisibility(View.GONE);
                break;
            }
            case USB_COM: {
                viewBinding.btnTestServer.setVisibility(View.GONE);
                viewBinding.llSerialPortList.setVisibility(View.GONE);
                viewBinding.llUsbSerialPortList.setVisibility(View.VISIBLE);
                viewBinding.btnOpenUsbList.setVisibility(View.GONE);
                viewBinding.btnOpenBluetoothList.setVisibility(View.GONE);
                viewBinding.btnOpenWifiSetting.setVisibility(View.GONE);
                break;
            }
            case USB: {
                viewBinding.btnTestServer.setVisibility(View.GONE);

                viewBinding.llSerialPortList.setVisibility(View.GONE);
                viewBinding.llUsbSerialPortList.setVisibility(View.GONE);
                viewBinding.btnOpenUsbList.setVisibility(View.VISIBLE);
                viewBinding.btnOpenBluetoothList.setVisibility(View.GONE);
                viewBinding.btnOpenWifiSetting.setVisibility(View.GONE);
                break;
            }
            case BLUETOOTH: {
                viewBinding.btnTestServer.setVisibility(View.VISIBLE);
                viewBinding.llSerialPortList.setVisibility(View.GONE);
                viewBinding.llUsbSerialPortList.setVisibility(View.GONE);
                viewBinding.btnOpenUsbList.setVisibility(View.GONE);
                viewBinding.btnOpenBluetoothList.setVisibility(View.VISIBLE);
                viewBinding.btnOpenWifiSetting.setVisibility(View.GONE);
                break;
            }
            case WIFI: {
                viewBinding.btnTestServer.setVisibility(View.VISIBLE);
                viewBinding.llSerialPortList.setVisibility(View.GONE);
                viewBinding.llUsbSerialPortList.setVisibility(View.GONE);
                viewBinding.btnOpenUsbList.setVisibility(View.GONE);
                viewBinding.btnOpenBluetoothList.setVisibility(View.GONE);
                viewBinding.btnOpenWifiSetting.setVisibility(View.VISIBLE);
                break;
            }
            default:
                break;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_open_serial_port_list: {
                Intent serverIntent = new Intent(this, SerialPortListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                break;
            }
            case R.id.btn_open_serial_port_config:
            case R.id.btn_open_usb_serial_port_config: {
                openActivity(SerialPortSettingActivity.class);
                break;
            }
            case R.id.btn_open_usb_list:
            case R.id.btn_open_usb_serial_port_list: {
                Intent serverIntent = new Intent(this, UsbListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                break;
            }

            case R.id.btn_open_bluetooth_list: {
                Intent serverIntent = new Intent(this, BluetoothListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                break;
            }
            case R.id.btn_open_wifi_setting: {
                openActivity(WifiSettingActivity.class);
                break;
            }

            case R.id.btn_connect: {
                connect();
                break;
            }
            case R.id.btn_disconnect: {
                disconnect(v);
                break;
            }
            case R.id.btn_clear: {
                viewBinding.tvMsg.setText("");
                viewBinding.sendText.setText("");
                break;
            }
            case R.id.btn_send: {
                send();
                break;
            }
            case R.id.btn_test_server: {
                //????????????????????????????????????????????????????????????
                PermissionsManager.getInstance().requestSpecialPermission(SpecialPermission.NOTIFICATION_ACCESS,this, new PermissionsResultAction() {
                    @Override
                    public void onGranted() {
                        if (connectWay == ConnectWay.BLUETOOTH){ //????????????????????????
                            bluetoothServiceIntent = new Intent(MainActivity.this, BluetoothService.class);
                            startService(bluetoothServiceIntent);
                        }else if(connectWay == ConnectWay.WIFI){ //????????????????????????
                            wifiServiceIntent = new Intent(MainActivity.this, WifiService.class);
                            startService(wifiServiceIntent);
                        }else {
                            showMsg("??????????????????????????????????????????????????????");
                        }
                    }

                    @Override
                    public void onDenied(String permission) {
                        LogUtils.i("???????????????" + permission);
                    }
                });


                break;
            }
            default:
                break;
        }
    }



    private void send() {
        if (baseConnectProvider == null) {
            showMsg("??????????????????");
            return;
        }
        String sendDataStr = viewBinding.sendText.getText().toString().trim();
        if (TextUtils.isEmpty(sendDataStr)) {
            showMsg("????????????????????????");
            return;
        }
        hideKeyBoard(this,viewBinding.sendText);
        MyApplication.getExecutor().execute(new Runnable() {
            @Override
            public void run() {

                byte[] sendData;
                try {
                    if (sendFormat){
                        sendData = ByteUtils.hexStringToByteArr(sendDataStr);
                        if (sendData.length == 0){
                            showMsg("16??????????????????");
                            return;
                        }
                    }else {
                        sendData = sendDataStr.getBytes();
                    }
                    
                    showMsg("??????" + sendDataStr,false);
                    byte[] buffer = new byte[256];
                    int read = baseConnectProvider.read(sendData, buffer, 5 * 1000);
                    if (read > 0) {
                        byte[] resultData = new byte[read];
                        System.arraycopy(buffer, 0, resultData, 0, read);
                       
                        if (receiveFormat){
                            showMsg("??????" + ByteUtils.byteArrToHexString(resultData),true);
                        }else {
                            showMsg("??????" + new String(resultData),true);
                        }
                    } else {
                        showMsg("?????????????????????" + read);
                    }
                } catch (Exception e) {
                    showMsg("???????????????" + e.getMessage());
                }

            }
        });

    }


    private void connect() {
        if (baseConnectProvider == null) {
            if (connectWay == ConnectWay.WIFI){
                WifiInfo wifiInfo = MyApplication.getWifiInfo();
                baseConnectProvider = new WifiConnectProvider(wifiInfo.getIp(),wifiInfo.getPort(),5000,5000);
            }else {
                Toast.makeText(mContext, "??????????????????", Toast.LENGTH_SHORT).show();
                return;
            }
        }


        showLoading();
        MyApplication.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                int ret = baseConnectProvider.open();
                if (ret == ErrorCode.ERROR_OK) {
                    showMsg("??????????????????");
                } else {
                    showMsg("??????????????????:"+ret);
                }
            }
        });

    }

    private void disconnect(View v) {
        if (baseConnectProvider != null) {
            baseConnectProvider.close();
            baseConnectProvider = null;
            if (v != null){ //?????????????????????????????????
                showMsg("????????????????????????");
            }

        }
    }
    private void showMsg(String s) {
        showMsg(s,null);
    }

    private void showMsg(String s,Boolean flag) {
        if (isDestroy(this)) {
            return;
        }
        MyApplication.getMainThreadExecutor().post(() -> {
            Date curDate = new Date(System.currentTimeMillis());
            String strDate = new SimpleDateFormat("HH:mm:ss.SSS").format(curDate);
            String log  = strDate + ":  " + s;
            if (flag == null){
                viewBinding.tvMsg.append(log+"\n");
            }else if(!flag){
                SpannableStringBuilder spn = new SpannableStringBuilder();
                spn.append(log).append("\n");
                spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorSendText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                viewBinding.tvMsg.append(spn);
            }else {
                SpannableStringBuilder spn = new SpannableStringBuilder();
                spn.append(log).append("\n");
                spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorReceiveText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                viewBinding.tvMsg.append(spn);
            }

            viewBinding.nestedScrollView.post(() -> {
                viewBinding.nestedScrollView.fullScroll(View.FOCUS_DOWN);//????????????
                hideLoading();
            });
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    if (extras == null) {
                        return;
                    }
                    disconnect(null);
                    //????????????????????????????????????????????????????????????
                    switch (connectWay) {
                        case SERIAL_PORT: {
                            devicePath = extras.getString(EXTRA_DEVICE_ITEM); //??????getString
                            SerialPortInfo serialPortInfo = MyApplication.getSerialPortInfo();
                            serialPortInfo.setDevicePath(devicePath);
                            baseConnectProvider = new SerialPortConnectProvider(MyApplication.getSerialPortConfig(serialPortInfo));
                            showMsg("??????????????????????????????" + devicePath);
                            break;
                        }
                        case USB_COM: {
                            usbDevice = extras.getParcelable(EXTRA_DEVICE_ITEM);
                            SerialPortInfo serialPortInfo = MyApplication.getSerialPortInfo();
                            baseConnectProvider = new UsbComConnectProvider(usbDevice, MyApplication.getSerialPortConfig(serialPortInfo));
                            showMsg("???????????????Usb?????????" + usbDevice.getDeviceName() + ":" + usbDevice.getVendorId() + ":" + usbDevice.getProductId());
                            break;
                        }
                        case USB: {
                            usbDevice = extras.getParcelable(EXTRA_DEVICE_ITEM);
                            baseConnectProvider = new UsbConnectProvider(usbDevice);
                            showMsg("???????????????Usb?????????" + usbDevice.getDeviceName() + ":" + usbDevice.getVendorId() + ":" + usbDevice.getProductId());
                            break;
                        }
                        case BLUETOOTH: {
                            bluetoothDevice = extras.getParcelable(EXTRA_DEVICE_ITEM);
                            baseConnectProvider = new BluetoothConnectProvider(bluetoothDevice);
                            showMsg("??????????????????????????????" + bluetoothDevice.getName() + ":" + bluetoothDevice.getAddress());
                            break;
                        }
                        case WIFI: {
                            //????????????????????????
                            break;
                        }
                        default:
                            break;
                    }
                }
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        UsbHelper.getInstance().unregisterReceiver();
        BluetoothHelper.getInstance().unregisterReceiver();

        disconnect(null);
        if (bluetoothServiceIntent != null){
            stopService(bluetoothServiceIntent);
            bluetoothServiceIntent = null;
        }
        if (wifiServiceIntent != null){
            stopService(wifiServiceIntent);
            wifiServiceIntent = null;
        }

    }

    @Override
    public void _onMessageEvent(MessageEvent messageEvent) {
        if (messageEvent.getCode() == 1000){
            showMsg((String) messageEvent.getEvent());
        }
    }

    public static void hideKeyBoard(Context context, EditText editText) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }
}

