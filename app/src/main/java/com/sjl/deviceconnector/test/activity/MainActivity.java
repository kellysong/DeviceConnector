package com.sjl.deviceconnector.test.activity;


import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
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
import android.widget.RadioGroup;
import android.widget.Toast;

import com.sjl.core.permission.PermissionsManager;
import com.sjl.core.permission.PermissionsResultAction;
import com.sjl.core.permission.SpecialPermission;
import com.sjl.core.util.log.LogUtils;
import com.sjl.deviceconnector.ErrorCode;
import com.sjl.deviceconnector.device.bluetooth.BluetoothHelper;
import com.sjl.deviceconnector.device.bluetooth.ble.BluetoothLeNotifyListener;
import com.sjl.deviceconnector.device.bluetooth.ble.BluetoothLeServiceListener;
import com.sjl.deviceconnector.device.bluetooth.ble.request.BluetoothLeRequest;
import com.sjl.deviceconnector.device.bluetooth.ble.request.CharacteristicWriteRequest;
import com.sjl.deviceconnector.device.bluetooth.ble.request.MtuRequest;
import com.sjl.deviceconnector.device.bluetooth.ble.request.NotifyRequest;
import com.sjl.deviceconnector.device.bluetooth.ble.response.BluetoothLeResponse;
import com.sjl.deviceconnector.device.usb.UsbHelper;
import com.sjl.deviceconnector.listener.ConnectedListener;
import com.sjl.deviceconnector.listener.DataReceivedListener;
import com.sjl.deviceconnector.listener.UsbPlugListener;
import com.sjl.deviceconnector.provider.BaseConnectProvider;
import com.sjl.deviceconnector.provider.BaseIoConnectProvider;
import com.sjl.deviceconnector.provider.BluetoothConnectProvider;
import com.sjl.deviceconnector.provider.BluetoothLeConnectProvider;
import com.sjl.deviceconnector.provider.SerialPortConnectProvider;
import com.sjl.deviceconnector.provider.UsbComConnectProvider;
import com.sjl.deviceconnector.provider.UsbConnectProvider;
import com.sjl.deviceconnector.provider.WifiConnectProvider;
import com.sjl.deviceconnector.test.BuildConfig;
import com.sjl.deviceconnector.test.R;
import com.sjl.deviceconnector.test.app.MyApplication;
import com.sjl.deviceconnector.test.databinding.MainActivityBinding;
import com.sjl.deviceconnector.test.entity.ConnectWay;
import com.sjl.deviceconnector.test.entity.MessageEvent;
import com.sjl.deviceconnector.test.entity.SerialPortInfo;
import com.sjl.deviceconnector.test.entity.WifiInfo;
import com.sjl.deviceconnector.test.service.BleService;
import com.sjl.deviceconnector.test.service.BluetoothService;
import com.sjl.deviceconnector.test.service.ReadThread;
import com.sjl.deviceconnector.test.service.WifiService;
import com.sjl.deviceconnector.test.util.MessageEventUtils;
import com.sjl.deviceconnector.util.ByteUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class MainActivity extends BaseActivity<MainActivityBinding> implements View.OnClickListener {
    private static final int REQUEST_CONNECT_DEVICE = 100;

    public static String EXTRA_DEVICE_ITEM = "device_item";
    public static String EXTRA_SCAN_FLAG = "scan_flag";

    private ConnectWay connectWay = ConnectWay.SERIAL_PORT;
    private BaseConnectProvider baseConnectProvider;
    private String devicePath;
    private UsbDevice usbDevice;
    private BluetoothDevice bluetoothDevice;
    private String[] permissions = new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION};
    private Intent bluetoothServiceIntent,wifiServiceIntent,bleServiceIntent;
    private boolean isServer = false;

    @Override
    protected void initView() {
        viewBinding.rgFormat.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                viewBinding.sendText.setText("");
                switch (checkedId){
                    case R.id.rb_text:
                        viewBinding.sendText.setInputType(InputType.TYPE_CLASS_TEXT);
                        break;
                    case R.id.rb_hex:
                        String digits = "0123456789abcdefABCDEF";
                        viewBinding.sendText.setKeyListener(DigitsKeyListener.getInstance(digits));
                        break;
                }
            }
        });


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
        viewBinding.btnStartTestServer.setOnClickListener(this);
        viewBinding.btnStopTestServer.setOnClickListener(this);

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

        //当调用baseConnectProvider.close();过几秒才会触发
        BluetoothHelper.getInstance().setConnectedListener(new ConnectedListener<BluetoothDevice>() {
            @Override
            public void onResult(BluetoothDevice device, boolean connected) {
                if (!connected){
                    Toast.makeText(MainActivity.this, "蓝牙设备" + device.getName() + "已断开连接", Toast.LENGTH_LONG).show();
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
                initDataWithPermission();
            }

            @Override
            public void onDenied(String permission) {
                LogUtils.i("拒绝权限：" + permission);
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
                isServer = false;
                viewBinding.llServer.setVisibility(View.GONE);
                viewBinding.llSerialPortList.setVisibility(View.VISIBLE);
                viewBinding.llUsbSerialPortList.setVisibility(View.GONE);
                viewBinding.btnOpenUsbList.setVisibility(View.GONE);
                viewBinding.btnOpenBluetoothList.setVisibility(View.GONE);
                viewBinding.btnOpenWifiSetting.setVisibility(View.GONE);
                break;
            }
            case USB_COM: {
                isServer = false;
                viewBinding.llServer.setVisibility(View.GONE);
                viewBinding.llSerialPortList.setVisibility(View.GONE);
                viewBinding.llUsbSerialPortList.setVisibility(View.VISIBLE);
                viewBinding.btnOpenUsbList.setVisibility(View.GONE);
                viewBinding.btnOpenBluetoothList.setVisibility(View.GONE);
                viewBinding.btnOpenWifiSetting.setVisibility(View.GONE);
                break;
            }
            case USB: {
                isServer = false;
                viewBinding.llServer.setVisibility(View.GONE);

                viewBinding.llSerialPortList.setVisibility(View.GONE);
                viewBinding.llUsbSerialPortList.setVisibility(View.GONE);
                viewBinding.btnOpenUsbList.setVisibility(View.VISIBLE);
                viewBinding.btnOpenBluetoothList.setVisibility(View.GONE);
                viewBinding.btnOpenWifiSetting.setVisibility(View.GONE);
                break;
            }
            case BLUETOOTH_LOW_ENERGY:
            case BLUETOOTH: {
                viewBinding.llServer.setVisibility(View.VISIBLE);
                viewBinding.llSerialPortList.setVisibility(View.GONE);
                viewBinding.llUsbSerialPortList.setVisibility(View.GONE);
                viewBinding.btnOpenUsbList.setVisibility(View.GONE);
                viewBinding.btnOpenBluetoothList.setVisibility(View.VISIBLE);
                viewBinding.btnOpenWifiSetting.setVisibility(View.GONE);
                break;
            }
            case WIFI: {
                viewBinding.llServer.setVisibility(View.VISIBLE);
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
                if (connectWay == ConnectWay.BLUETOOTH_LOW_ENERGY){
                    serverIntent.putExtra(EXTRA_SCAN_FLAG,true);
                }else {
                    serverIntent.putExtra(EXTRA_SCAN_FLAG,false);
                }
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

                if (!isServer){
                    send();
                }else {
                    sendClient();
                }
                break;
            }
            case R.id.btn_start_test_server: {
                //需要申请一个前台通知，防止高版本服务挂掉
                PermissionsManager.getInstance().requestSpecialPermission(SpecialPermission.NOTIFICATION_ACCESS,this, new PermissionsResultAction() {
                    @Override
                    public void onGranted() {
                        if (connectWay == ConnectWay.BLUETOOTH){ //启动模拟测试服务
                            stopBleService();
                            bluetoothServiceIntent = new Intent(MainActivity.this, BluetoothService.class);
                            startService(bluetoothServiceIntent);
                            isServer = true;
                            viewBinding.llClient.setVisibility(View.GONE);
                            viewBinding.btnStartTestServer.setEnabled(false);
                            viewBinding.btnStopTestServer.setEnabled(true);
                        }else if(connectWay == ConnectWay.WIFI){ //启动模拟测试服务
                            wifiServiceIntent = new Intent(MainActivity.this, WifiService.class);
                            startService(wifiServiceIntent);
                            isServer = true;
                            viewBinding.llClient.setVisibility(View.GONE);
                            viewBinding.btnStartTestServer.setEnabled(false);
                            viewBinding.btnStopTestServer.setEnabled(true);
                        }else if(connectWay == ConnectWay.BLUETOOTH_LOW_ENERGY){ //启动模拟测试服务
                            stopBluetoothService();
                            bleServiceIntent = new Intent(MainActivity.this, BleService.class);
                            startService(bleServiceIntent);
                            isServer = true;
                            viewBinding.llClient.setVisibility(View.GONE);
                            viewBinding.btnStartTestServer.setEnabled(false);
                            viewBinding.btnStopTestServer.setEnabled(true);
                        }else {
                            showMsg("该连接方式不支持，请检查");
                        }

                    }

                    @Override
                    public void onDenied(String permission) {
                        LogUtils.i("拒绝权限：" + permission);
                    }
                });
                
                break;
            }
            case R.id.btn_stop_test_server: {
                if (connectWay == ConnectWay.BLUETOOTH){ //启动模拟测试服务
                    stopBluetoothService();
                    isServer = false;
                    viewBinding.llClient.setVisibility(View.VISIBLE);
                    viewBinding.btnStartTestServer.setEnabled(true);
                    viewBinding.btnStopTestServer.setEnabled(false);
                }else if(connectWay == ConnectWay.WIFI){ //启动模拟测试服务
                    stopWifiService();
                    isServer = false;
                    viewBinding.llClient.setVisibility(View.VISIBLE);
                    viewBinding.btnStartTestServer.setEnabled(true);
                    viewBinding.btnStopTestServer.setEnabled(false);
                }else if(connectWay == ConnectWay.BLUETOOTH_LOW_ENERGY){ //启动模拟测试服务
                    stopBleService();
                    isServer = false;
                    viewBinding.llClient.setVisibility(View.VISIBLE);
                    viewBinding.btnStartTestServer.setEnabled(true);
                    viewBinding.btnStopTestServer.setEnabled(false);
                }else {
                    showMsg("该连接方式不支持，请检查");
                }

                break;
            }
            default:
                break;
        }
    }

    /**
     * 服务端发给客户端
     */
    private void sendClient() {

        String sendDataStr = viewBinding.sendText.getText().toString().trim();
        if (TextUtils.isEmpty(sendDataStr)) {
            showMsg("发送数据不能为空");
            return;
        }
        hideKeyBoard(this,viewBinding.sendText);
        MyApplication.getExecutor().execute(new Runnable() {
            @Override
            public void run() {

                byte[] sendData;
                try {
                    if (viewBinding.rbHex.isChecked()){
                        sendData = ByteUtils.hexStringToByteArr(sendDataStr);
                        if (sendData.length == 0){
                            showMsg("16进制格式错误");
                            return;
                        }
                    }else {
                        sendData = sendDataStr.getBytes();
                    }

                    showMsg("发：" + sendDataStr,false);
                    MessageEventUtils.sendData(connectWay.ordinal(),new String(sendData));

                } catch (Exception e) {
                    LogUtils.e(e);
                    showMsg("操作异常：" + e.getMessage());
                }

            }
        });
    }



    /**
     * 客户端发给服务端
     */
    private void send() {
        if (baseConnectProvider == null) {
            showMsg("请先连接设备");
            return;
        }
        String sendDataStr = viewBinding.sendText.getText().toString().trim();
        if (TextUtils.isEmpty(sendDataStr)) {
            showMsg("发送数据不能为空");
            return;
        }
        hideKeyBoard(this,viewBinding.sendText);
        if (baseConnectProvider instanceof BluetoothLeConnectProvider){
            bleSend(sendDataStr,baseConnectProvider);
            return;
        }
        MyApplication.getExecutor().execute(new Runnable() {
            @Override
            public void run() {

                byte[] sendData;
                try {
                    if (viewBinding.rbHex.isChecked()){
                        sendData = ByteUtils.hexStringToByteArr(sendDataStr);
                        if (sendData.length == 0){
                            showMsg("16进制格式错误");
                            return;
                        }
                    }else {
                        sendData = sendDataStr.getBytes();
                    }

                    showMsg("发：" + sendDataStr,false);
                    byte[] buffer = new byte[256];
                    int read;
              /*      if (baseConnectProvider instanceof BaseIoConnectProvider){
                        read = ((BaseIoConnectProvider) baseConnectProvider).readBlocking(sendData, buffer, 5 * 1000);
                    }else {
                        read = baseConnectProvider.read(sendData, buffer, 5 * 1000);
                    }*/

                    read = baseConnectProvider.read(sendData, buffer, 5 * 1000);

                    if (read > 0) {
                        byte[] resultData = new byte[read];
                        System.arraycopy(buffer, 0, resultData, 0, read);

                        if (viewBinding.rbHex.isChecked()){
                            showMsg("收：" + ByteUtils.byteArrToHexString(resultData),true);
                        }else {
                            showMsg("收：" + new String(resultData),true);
                        }
                    } else {
                        showMsg("读取数据失败：" + read);
                    }
                } catch (Exception e) {
                    LogUtils.e(e);
                    showMsg("操作异常：" + e.getMessage());
                }

            }
        });
    }

    /**
     * 测试服务id,配上自己的uuid即可
     */
    private final UUID UUID_SERVICE= UUID.fromString(BuildConfig.uuid_service);
    private final UUID UUID_CHARACTER_READ = UUID.fromString(BuildConfig.uuid_character_read);
    private final UUID UUID_CHARACTER_WRITE = UUID.fromString(BuildConfig.uuid_character_write);
    private boolean modifyMtuFlag = false;
    private void bleSend(String sendDataStr, BaseConnectProvider baseConnectProvider) {


        MyApplication.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (!modifyMtuFlag){
                    //修改mtu
                    modifyMtu();
                    modifyMtuFlag = true;
                }


                byte[] sendData;
                try {
                    if (viewBinding.rbHex.isChecked()){
                        sendData = ByteUtils.hexStringToByteArr(sendDataStr);
                        if (sendData.length == 0){
                            showMsg("16进制格式错误");
                            return;
                        }
                    }else {
                        sendData = sendDataStr.getBytes();
                    }
                    showMsg("发：" + sendDataStr,false);
                    CharacteristicWriteRequest bluetoothLeRequest = new CharacteristicWriteRequest();
                    bluetoothLeRequest.setService(UUID_SERVICE);
                    bluetoothLeRequest.setCharacter(UUID_CHARACTER_WRITE);
                    bluetoothLeRequest.setBytes(sendData);
                    BluetoothLeResponse response = new BluetoothLeResponse();

                    sendBleRequest(baseConnectProvider,bluetoothLeRequest,response);

                    showMsg("收：" + response,true);

                } catch (Exception e) {
                    LogUtils.e(e);
                    showMsg("操作异常：" + e.getMessage());
                }

            }
        });
    }

    private void initNotify() {
        NotifyRequest notifyRequest = new NotifyRequest();
        notifyRequest.setService(UUID_SERVICE);
        notifyRequest.setCharacter(UUID_CHARACTER_READ);
        //开
        notifyRequest.setEnable(true);
        BluetoothLeResponse response = new BluetoothLeResponse();
        try {
            sendBleRequest(baseConnectProvider,notifyRequest,response);
            showMsg("Ble通知注册结果：" + response);
        } catch (Exception e) {
            LogUtils.e("Ble通知注册异常",e);
        }
    }

    private void modifyMtu() {
        MtuRequest mtuRequest = new MtuRequest();
        mtuRequest.setMtu(240);
        BluetoothLeResponse response = new BluetoothLeResponse();
        try {
            sendBleRequest(baseConnectProvider,mtuRequest,response);
            showMsg("Ble mtu修改结果：" + response);
        } catch (Exception e) {
            LogUtils.e("Ble mtu修改异常",e);
        }
    }

    private void sendBleRequest(BaseConnectProvider baseConnectProvider,BluetoothLeRequest bluetoothLeRequest, BluetoothLeResponse bluetoothLeResponse) throws Exception {
        BluetoothLeConnectProvider connectProvider = (BluetoothLeConnectProvider) baseConnectProvider;
        connectProvider.sendRequest(bluetoothLeRequest,bluetoothLeResponse,10*1000);
    }


    private void connect() {
        if (baseConnectProvider == null) {
            if (connectWay == ConnectWay.WIFI){
                WifiInfo wifiInfo = MyApplication.getWifiInfo();
                baseConnectProvider = new WifiConnectProvider(wifiInfo.getIp(),wifiInfo.getPort(),5000,5000);
            }else {
                Toast.makeText(mContext, "请先选择设备", Toast.LENGTH_SHORT).show();
                return;
            }
        }


        showLoading();
        MyApplication.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                int ret = baseConnectProvider.open();
                if (ret == ErrorCode.ERROR_OK) {
                    showMsg("设备连接成功");
                    if (baseConnectProvider instanceof BluetoothLeConnectProvider){
                        //监听通知信息
                        BluetoothLeConnectProvider bluetoothLeConnectProvider = (BluetoothLeConnectProvider) baseConnectProvider;
                        //服务发现监听
                        bluetoothLeConnectProvider.setBluetoothLeServiceListener(new BluetoothLeServiceListener() {
                            @Override
                            public void onServicesDiscovered(int status, List<BluetoothGattService> bluetoothGattServices) {
                                showMsg("status：" + status + ",bluetoothGattServices:" + bluetoothGattServices.size());
                                if (bluetoothGattServices.size() > 0){
                                    initNotify();
                                }
                            }
                        });
                        //服务通知监听
                        bluetoothLeConnectProvider.setBluetoothLeNotifyListener(new BluetoothLeNotifyListener() {
                            @Override
                            public void onNotify(UUID serviceId, UUID characterId, byte[] value) {
                                showMsg("收到服务端信息：" + new String(value));
                            }
                        });

                    }
                    //单独监听就不能使用baseConnectProvider.read接口了
               /*     else if (baseConnectProvider instanceof BaseIoConnectProvider){
                        startReadThread((BaseIoConnectProvider)baseConnectProvider);
                    }*/
                } else {
                    showMsg("设备连接失败:"+ret);
                    if (baseConnectProvider instanceof BluetoothLeConnectProvider){
                        baseConnectProvider.close();
                    }
                }
            }
        });

    }



    private void disconnect(View v) {
        if (baseConnectProvider != null) {
            baseConnectProvider.close();
            if (v != null){ //收到点击点开才显示日志
                showMsg("设备断开连接成功");
            }
            modifyMtuFlag = false;

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
                viewBinding.nestedScrollView.fullScroll(View.FOCUS_DOWN);//滚到底部
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
                    //根据选择的连接方式，初始化不同的连接实例
                    switch (connectWay) {
                        case SERIAL_PORT: {
                            devicePath = extras.getString(EXTRA_DEVICE_ITEM); //注意getString
                            SerialPortInfo serialPortInfo = MyApplication.getSerialPortInfo();
                            serialPortInfo.setDevicePath(devicePath);
                            baseConnectProvider = new SerialPortConnectProvider(MyApplication.getSerialPortConfig(serialPortInfo));
                            showMsg("当前选择的串口设备：" + devicePath);
                            break;
                        }
                        case USB_COM: {
                            usbDevice = extras.getParcelable(EXTRA_DEVICE_ITEM);
                            SerialPortInfo serialPortInfo = MyApplication.getSerialPortInfo();
                            baseConnectProvider = new UsbComConnectProvider(usbDevice, MyApplication.getSerialPortConfig(serialPortInfo));
                            showMsg("当前选择的Usb设备：" + usbDevice.getDeviceName() + ":" + usbDevice.getVendorId() + ":" + usbDevice.getProductId());
                            break;
                        }
                        case USB: {
                            usbDevice = extras.getParcelable(EXTRA_DEVICE_ITEM);
                            baseConnectProvider = new UsbConnectProvider(usbDevice);
                            showMsg("当前选择的Usb设备：" + usbDevice.getDeviceName() + ":" + usbDevice.getVendorId() + ":" + usbDevice.getProductId());
                            break;
                        }
                        case BLUETOOTH: {
                            bluetoothDevice = extras.getParcelable(EXTRA_DEVICE_ITEM);
                            baseConnectProvider = new BluetoothConnectProvider(bluetoothDevice);
                            showMsg("当前选择的蓝牙设备：" + bluetoothDevice.getName() + ":" + bluetoothDevice.getAddress());
                            break;
                        }
                        case WIFI: {
                            //在连接按钮初始化
                            break;
                        }
                        case BLUETOOTH_LOW_ENERGY: {
                            bluetoothDevice = extras.getParcelable(EXTRA_DEVICE_ITEM);
                            baseConnectProvider = new BluetoothLeConnectProvider(bluetoothDevice);
                            showMsg("当前选择的蓝牙设备：" + bluetoothDevice.getName() + ":" + bluetoothDevice.getAddress());
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
        stopBluetoothService();
        stopBleService();
        stopWifiService();

    }
    private void stopBluetoothService() {
        if (bluetoothServiceIntent != null){
            stopService(bluetoothServiceIntent);
            bluetoothServiceIntent = null;
        }
    }

    private void stopBleService() {
        if (bleServiceIntent != null){
            stopService(bleServiceIntent);
            bleServiceIntent = null;
        }

    }
    private void stopWifiService() {
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


    public void startReadThread(BaseIoConnectProvider baseIoConnectProvider) {
        baseIoConnectProvider.startReadThread(BaseIoConnectProvider.SPLICING);
        baseIoConnectProvider.setDataReceivedListener(new DataReceivedListener() {
            @Override
            public void onDataReceived(byte[] data) {
                showMsg("接收到数据:" + ByteUtils.byteArrToHexString(data));
            }
        });
    }


}

