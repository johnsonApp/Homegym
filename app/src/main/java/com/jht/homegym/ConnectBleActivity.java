package com.jht.homegym;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jht.homegym.ble.BleActivity;
import com.jht.homegym.ble.MultipleBleService;
import com.jht.homegym.ble.Constants;
import com.jht.homegym.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class ConnectBleActivity extends BleActivity implements View.OnClickListener{

    private static final String TAG = "ConnectBleActivity";

/*    private static final int RETRY_TIME = 3;//3;
    private static final String THINGYNAME = "Thingy";
    private static final String HOMEGYEMNAME = "HomeGYM Console";
    private static final String ADDRESS = "address";
    private static final String DEVICE_NAME ="name";
    private static final String IS_CONNECTED ="isConnect";

    private UUID[] AccessoryUUID = {Utils.THINGY_BASE_UUID};
    private UUID[] HomegymUUID = {Utils.HOMEGYM_BASE_UUID};
*/
    private ImageView mClosePage;
    private Button mConnect;
    private ImageView mConnectIcon;
    private TextView mConnectLabel;
    private LinearLayout mTipsLayout;

    //Constant
/*    public static final int SERVICE_BIND = 1;
    public static final int CONNECT_CHANGE = 2;


    //Member fields
    private boolean mIsBind;
    private MultipleBleService mBleService;
    private List<Map<String, Object>> mDeviceList;
    private boolean mIsConnected = false;
    private boolean mIsHomegymScan = false;
    private boolean mIsThingyScan = false;

    private int mRetryTime = 0;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBleService = ((MultipleBleService.LocalBinder) service).getService();
            mIsBind = true;
            if (mBleService != null) mHandler.sendEmptyMessage(SERVICE_BIND);
            if (mBleService.initialize()) {
                if (mBleService.enableBluetooth(true)) {
                    if(Utils.handleVersionPermission(ConnectBleActivity.this)){
                        setScan(true);
                    }
                    Toast.makeText(ConnectBleActivity.this, "Bluetooth was opened", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ConnectBleActivity.this, "not support Bluetooth", Toast.LENGTH_SHORT).show();
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
                    mConnectLable.setText(getString(R.string.dev_conn_number) +
                            mBleService.getConnectDevices().size());
                    Log.i(TAG, "handleMessage: " + mBleService.getConnectDevices().toString());
                    break;
            }
        }
    };

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
                }
                mHandler.sendEmptyMessage(CONNECT_CHANGE);
                dismissDialog();
                updateUI();
            } else if (intent.getAction().equals(Constants.ACTION_GATT_DISCONNECTED)) {
                Log.i(TAG, "onReceive: DISCONNECTED: " + mBleService.getConnectDevices().size());
                if(mBleService.getConnectDevices().size() == 0){
                    mIsConnected = false;
                }
                mHandler.sendEmptyMessage(CONNECT_CHANGE);
                dismissDialog();
                updateUI();
            } else if (intent.getAction().equals(Constants.ACTION_SCAN_FINISHED)) {
                //btn_scanBle.setEnabled(true);
                Log.i(TAG,"ACTION_SCAN_FINISHED");
                isScanDevice();
                Log.d(TAG," mIsThingyScan " + mIsThingyScan + " mIsHomegymScan " + mIsHomegymScan);
                if((mIsHomegymScan && mIsThingyScan) || mRetryTime >= RETRY_TIME || mDeviceList.size() == 2) {
                    mConnect.setEnabled(true);
                    dismissDialog();
                    connectDevice();
                }else {
                    setScan(true);
                    mRetryTime ++;
                }
            }
        }
    };*/

    public void updateUI(int status){
        switch (status){
            case Constants.STATE_CONNECTED:
                if (mIsConnected) {
                    mConnectIcon.setImageDrawable(getResources().getDrawable(R.drawable.connect_ble_success));
                    mConnectLabel.setText(getResources().getString(R.string.connect_ble_tips1));
                    mTipsLayout.setVisibility(View.INVISIBLE);
                    mConnect.setText(getResources().getString(R.string.start_training));
                } else {
                    mConnectIcon.setImageDrawable(getResources().getDrawable(R.drawable.connect_ble_failed));
                    mConnectLabel.setText(getResources().getString(R.string.connect_ble_tips));
                    mTipsLayout.setVisibility(View.INVISIBLE);
                }
                break;
            case Constants.STATE_DISCONNECTED:
                mConnectIcon.setImageDrawable(getResources().getDrawable(R.drawable.connect_ble_failed));
                mConnectLabel.setText(getResources().getString(R.string.connect_ble_tips));
                mTipsLayout.setVisibility(View.INVISIBLE);
                break;
            case Constants.STATE_SCAN_FINISH:
                mConnect.setEnabled(true);
                break;
        }
    }

/*    private static IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_BLUETOOTH_DEVICE);
        intentFilter.addAction(Constants.ACTION_GATT_CONNECTED);
        intentFilter.addAction(Constants.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(Constants.ACTION_SCAN_FINISHED);
        return intentFilter;
    }*/



    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_ble);
        mClosePage = findViewById(R.id.close_page);
        mClosePage.setOnClickListener(this);
        mConnect = findViewById(R.id.button_connect_ble);
        mConnect.setOnClickListener(this);
        mConnectIcon = findViewById(R.id.connect_status_icon);
        mConnectLabel = findViewById(R.id.connect_label);
        mTipsLayout = (LinearLayout) findViewById(R.id.tips_layout);

        /*mDeviceList = new ArrayList<Map<String, Object>>();
        registerReceiver(mBleReceiver, makeIntentFilter());
        doBindService();*/
    }


/*    public void onBackPressed() {
        if (mBleService.isScanning()) {
            mBleService.scanLeDevice(false);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBleService.isScanning()) {
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
    }*/

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.close_page:
                finish();
                break;
            case R.id.button_connect_ble:
                if(mIsConnected) {
                    startActivity(new Intent(this, FreeModeSelectActivity.class));
                    finish();
                }else {
                    mRetryTime = 0;
                    setScan(true);
                }
                break;
        }
    }

/*    private void connectDevice() {
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


    private void setScan(boolean scan){
        if(null == mProgressDialog || !mProgressDialog.isShowing()) {
            showDialog(getResources().getString(R.string.scanning));
        }
        if(!mIsHomegymScan){
            mBleService.scanLeDevice(scan,HomegymUUID);
        }else if(!mIsThingyScan){
            mBleService.scanLeDevice(scan, AccessoryUUID);
        }
        mConnect.setEnabled(!scan);
    }

    private void isScanDevice(){
        int size = mDeviceList.size();
        for (int i = 0; i < size; i++) {
            HashMap<String, Object> devMap = (HashMap<String, Object>) mDeviceList.get(i);
            String name = (String )(devMap.get(DEVICE_NAME));
            if(name.equals(HOMEGYEMNAME)){
                mIsHomegymScan = true;
            }else if(name.equals(THINGYNAME)){
                mIsThingyScan = true;
            }
        }
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
    }*/
}
