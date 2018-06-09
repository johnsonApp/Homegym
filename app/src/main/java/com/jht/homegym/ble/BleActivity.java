package com.jht.homegym.ble;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.jht.homegym.ConnectBleActivity;
import com.jht.homegym.R;
import com.jht.homegym.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class BleActivity extends AppCompatActivity {
    private static final String TAG = "BleActivity";

    private static final int RETRY_TIME = 3;//3;
    private static final int RETRY_HOMEGYM_TIME = 1;
    private static final String THINGYNAME = "HomeGYM Accessory";
    private static final String HOMEGYEMNAME = "HomeGYM Console";
    private static final String ADDRESS = "address";
    private static final String DEVICE_NAME ="name";
    private static final String IS_CONNECTED ="isConnect";

    private UUID[] AccessoryUUID = {Utils.ACCESSORY_BASE_UUID};
    private UUID[] HomegymUUID = {Utils.HOMEGYM_BASE_UUID};

    public static final int SERVICE_BIND = 1;
    public static final int CONNECT_CHANGE = 2;

    private boolean mIsBind;
    private MultipleBleService mBleService;
    private List<Map<String, Object>> mDeviceList;
    private boolean mIsHomegymScan = false;
    private boolean mIsThingyScan = false;
    protected boolean mIsConnected = false;
    protected int mRetryTime = 0;


    protected BroadcastReceiver mBleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.ACTION_BLUETOOTH_DEVICE)) {
                String tmpDevName = intent.getStringExtra(DEVICE_NAME);
                String tmpDevAddress = intent.getStringExtra(ADDRESS);
                Log.i(TAG, "name: " + tmpDevName + ", address: " + tmpDevAddress);
                HashMap<String, Object> deviceMap = new HashMap<>();
                deviceMap.put(DEVICE_NAME, tmpDevName);
                deviceMap.put(ADDRESS, tmpDevAddress);
                deviceMap.put(IS_CONNECTED, false);
                mDeviceList.add(deviceMap);
                //mDeviceAdapter.notifyDataSetChanged();
            } else if (intent.getAction().equals(Constants.ACTION_GATT_CONNECTED)) {
                Log.i(TAG, "onReceive: CONNECTED: " + mBleService.getConnectDevices().size());
                if(mBleService.getConnectDevices().size() > 0){
                    mIsConnected = true;
                    connectChanged();
                }
                dismissDialog();
                updateUI(Constants.STATE_CONNECTED);
            } else if (intent.getAction().equals(Constants.ACTION_GATT_DISCONNECTED)) {
                Log.i(TAG, "onReceive: DISCONNECTED: " + mBleService.getConnectDevices().size());
                if(mBleService.getConnectDevices().size() == 0){
                    mIsConnected = false;
                    connectChanged();
                }
                dismissDialog();
                updateUI(Constants.STATE_DISCONNECTED);
            } else if (intent.getAction().equals(Constants.ACTION_SCAN_FINISHED)) {
                //btn_scanBle.setEnabled(true);
                Log.i(TAG,"ACTION_SCAN_FINISHED");
                isScanDevice();
                Log.d(TAG," mIsThingyScan " + mIsThingyScan + " mIsHomegymScan " + mIsHomegymScan);
                if((mIsHomegymScan && mIsThingyScan) || mRetryTime >= RETRY_TIME || mDeviceList.size() == 2) {
                    updateUI(Constants.STATE_SCAN_FINISH);
                    dismissDialog();
                    connectDevice();
                }else {
                    mRetryTime ++;
                    setScan(true);
                }
            }
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBleService = ((MultipleBleService.LocalBinder) service).getService();
            mIsBind = true;
            if (mBleService != null) mHandler.sendEmptyMessage(SERVICE_BIND);
            if (mBleService.initialize()) {
                if (mBleService.enableBluetooth(true)) {
                    if(Utils.handleVersionPermission(BleActivity.this)){
                        List<BluetoothDevice> list = mBleService.getConnectDevices();
                        if (list == null || list.size() == 0){
                            //Log.d(TAG,"onServiceConnected address " + list.get(0).getAddress());
                            if (mDeviceList == null || mDeviceList.size() == 0) {
                                setScan(true);
                            } else {
                                connectDevice();
                            }
                        }
                    }
                    Toast.makeText(BleActivity.this, "Bluetooth was opened", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(BleActivity.this, "not support Bluetooth", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBleService = null;
            mIsBind = false;
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SERVICE_BIND:
                    setBleServiceListener();
                    break;
                case CONNECT_CHANGE:
                    //deviceAdapter.notifyDataSetChanged();
                    Log.i(TAG, "handleMessage: " + mBleService.getConnectDevices().toString());
                    break;
            }
        }
    };

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDeviceList = new ArrayList<Map<String, Object>>();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mBleReceiver, makeIntentFilter());
        doBindService();
    }

    private void setBleServiceListener() {
        mBleService.setOnConnectListener(new MultipleBleService.OnConnectionStateChangeListener() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    for (int i = 0; i < mDeviceList.size(); i++) {
                        HashMap<String, Object> devMap = (HashMap<String, Object>) mDeviceList.get(i);
                        if (devMap.get(ADDRESS).toString().equals(gatt.getDevice().getAddress())) {
                            ((HashMap) mDeviceList.get(i)).put(IS_CONNECTED, false);
                            return;
                        }
                    }
                } else if (newState == BluetoothProfile.STATE_CONNECTING) {

                } else if (newState == BluetoothProfile.STATE_CONNECTED) {
                    for (int i = 0; i < mDeviceList.size(); i++) {
                        HashMap<String, Object> devMap = (HashMap<String, Object>) mDeviceList.get(i);
                        if (devMap.get(ADDRESS).toString().equals(gatt.getDevice().getAddress())) {
                            ((HashMap) mDeviceList.get(i)).put(IS_CONNECTED, true);
                            return;
                        }
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTING) {

                }
            }
        });
    }

    public void setScan(boolean scan){
        if(null == mProgressDialog || !mProgressDialog.isShowing()) {
            showDialog(getResources().getString(R.string.scanning));
        }
        if (scan && mBleService.isScanning()) {
            mBleService.scanLeDevice(false);
        }
        Log.d(TAG,"setScan " + mRetryTime);
        if(!mIsHomegymScan && mRetryTime < RETRY_HOMEGYM_TIME){
            mBleService.scanLeDevice(scan,HomegymUUID);
        }else if(!mIsThingyScan){
            mBleService.scanLeDevice(scan, AccessoryUUID);
        }

        if(!mBleService.isScanning()){
            dismissDialog();
            connectDevice();
        }
        //mConnect.setEnabled(!scan);
    }

    public void stopScan(){
        if (null != mBleService && mBleService.isScanning()) {
            mBleService.scanLeDevice(false);
        }
    }


    @Override
    public void onBackPressed() {
        if (null != mBleService && mBleService.isScanning()) {
            mBleService.scanLeDevice(false);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != mBleService && mBleService.isScanning()) {
            mBleService.scanLeDevice(false);
        }
        doUnBindService();
        unregisterReceiver(mBleReceiver);
    }

    private void doBindService() {
        Intent serviceIntent = new Intent(this, MultipleBleService.class);
        bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void doUnBindService() {
        if (mIsBind) {
            unbindService(mServiceConnection);
            mBleService = null;
            mIsBind = false;
        }
    }

    private static IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_BLUETOOTH_DEVICE);
        intentFilter.addAction(Constants.ACTION_GATT_CONNECTED);
        intentFilter.addAction(Constants.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(Constants.ACTION_SCAN_FINISHED);
        return intentFilter;
    }

    private ProgressDialog mProgressDialog;

    private void showDialog(String message) {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(mProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }

    private void dismissDialog() {
        if (mProgressDialog == null) return;
        mProgressDialog.dismiss();
        mProgressDialog = null;
    }


    private void isScanDevice(){
        int size = mDeviceList.size();
        for (int i = 0; i < size; i++) {
            HashMap<String, Object> devMap = (HashMap<String, Object>) mDeviceList.get(i);
            String name = (String )(devMap.get(DEVICE_NAME));
            if(null == name){
                return;
            }
            if(name.equals(HOMEGYEMNAME)){
                mIsHomegymScan = true;
            }else if(name.equals(THINGYNAME)){
                mIsThingyScan = true;
            }
        }
    }


    public void setConnect(){
        int size = mDeviceList.size();
        if(0 == size){
            setScan(true);
        }else {
            connectDevice();
        }
    }

    public void setDisconnect(){
        int size = mDeviceList.size();
        if(0 == size){
            dismissDialog();
            Log.i(TAG,"connectDevice make Toast");
            Toast.makeText(this,"Can not find any device ,please retry",Toast.LENGTH_LONG);
            return;
        }
        for (int i = 0; i < size; i++) {
            HashMap<String, Object> devMap = (HashMap<String, Object>) mDeviceList.get(i);
            Log.i(TAG,"connectDevice " + devMap.get(ADDRESS).toString());
            if((boolean)devMap.get(IS_CONNECTED)) {
                mBleService.disconnect(devMap.get(ADDRESS).toString());
            }
        }
        Log.i(TAG,"connectDevice " + size);
    }

    private void connectDevice() {
        if (mDeviceList != null){
            showDialog(getResources().getString(R.string.connecting));
            int size = mDeviceList.size();
            if(0 == size){
                dismissDialog();
                Log.i(TAG,"connectDevice make Toast");
                Toast.makeText(this,"Can not find any device ,please retry",Toast.LENGTH_LONG);
                return;
            }
            for (int i = 0; i < size; i++) {
                HashMap<String, Object> devMap = (HashMap<String, Object>) mDeviceList.get(i);
                Log.i(TAG,"connectDevice " + devMap.get(ADDRESS).toString());
                if(!(boolean)devMap.get(IS_CONNECTED)) {
                    mBleService.connect(devMap.get(ADDRESS).toString());
                }
            }
            Log.i(TAG,"connectDevice " + size);
        }

    }

    public void updateUI(int status){

    }

    public void connectChanged(){

    }
}
