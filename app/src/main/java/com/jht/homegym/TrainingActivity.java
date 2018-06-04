package com.jht.homegym;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jht.homegym.ble.MultipleBleService;
import com.jht.homegym.utils.CrcUtils;
import com.jht.homegym.utils.ThingyUtils;
import com.jht.homegym.algorithm.AccessoryExercise;

import java.util.HashMap;
import java.util.List;

public class TrainingActivity extends Activity {

    private static final String  TAG = "TrainingActivity";

    public static final int SERVICE_BIND = 1;
    public static final int UPDATE_COUNT = 2;

    private ImageView mBodyImage;
    private LinearLayout mSelectLayout;
    private int[] mBtnId = {R.id.button1, R.id.button2, R.id.button3, R.id.button4};
    private Button[] mButtons = new Button[mBtnId.length];
    private int mCurPart = 0;//当前选中的身体部分
    private TextView mCountDown;


    private boolean mIsBind;
    private MultipleBleService mBleService;
    private List<BluetoothDevice> mConnectedDevices;

    private AccessoryExercise mDumbbellExercise;
    private int mDumbbellExerciseCounter = 0;

    private BluetoothGattCharacteristic mMotionConfigurationCharacteristic;
    private BluetoothGattCharacteristic mTapCharacteristic;
    private BluetoothGattCharacteristic mOrientationCharacteristic;
    private BluetoothGattCharacteristic mQuaternionCharacteristic;
    private BluetoothGattCharacteristic mPedometerCharacteristic;
    private BluetoothGattCharacteristic mRawDataCharacteristic;
    private BluetoothGattCharacteristic mEulerCharacteristic;
    private BluetoothGattCharacteristic mRotationMatrixCharacteristic;
    private BluetoothGattCharacteristic mHeadingCharacteristic;
    private BluetoothGattCharacteristic mGravityVectorCharacteristic;

    private BluetoothGattCharacteristic mHomegymReadCharacteristic;
    private BluetoothGattCharacteristic mHomegymWriteCharacteristic;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SERVICE_BIND:
                    setBleServiceListener();
                    mConnectedDevices = mBleService.getConnectDevices();
                    discoveryServices();
                    break;
                case UPDATE_COUNT:
                    mCountDown.setText(String.valueOf(mDumbbellExerciseCounter));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
        setContentView(R.layout.activity_training);
        mBodyImage = (ImageView) findViewById(R.id.body_part);
        mSelectLayout = (LinearLayout) findViewById(R.id.select_layout);
        int length = mBtnId.length;
        for (int i = 0; i < length; i++){
            mButtons[i] = (Button) findViewById(mBtnId[i]);
            mButtons[i].setOnClickListener(listener);
        }
        mBodyImage.setOnClickListener(listener);
        //从上个页面获取curPart的值，然后给curPart赋值

        mCountDown = findViewById(R.id.count_down);

        mDumbbellExercise = new AccessoryExercise();
        doBindService();
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.body_part){
                if (mSelectLayout.getVisibility() == View.VISIBLE){
                    mSelectLayout.setVisibility(View.INVISIBLE);
                } else if (mSelectLayout.getVisibility() == View.INVISIBLE){
                    mSelectLayout.setVisibility(View.VISIBLE);
                }
            } else {
                int length = mBtnId.length;
                for (int i = 0; i < length; i++){
                    if (v.getId() == mBtnId[i]){
                        mButtons[mCurPart].setBackgroundColor(getResources().getColor(R.color.colorTitleBackground));
                        mButtons[mCurPart].setTextColor(getResources().getColor(android.R.color.black));
                        mCurPart = i;
                        mButtons[mCurPart].setBackgroundColor(getResources().getColor(R.color.colorButtonBackground));
                        mButtons[mCurPart].setTextColor(getResources().getColor(android.R.color.white));
                    }
                }
            }
        }
    };

    public void onStop(){
        super.onStop();
        doUnBindService();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBleService = ((MultipleBleService.LocalBinder) service).getService();
            Log.d(TAG,"onServiceConnected " + (mBleService == null));
            if(null != mBleService) {
                mHandler.sendEmptyMessage(SERVICE_BIND);
            }
            if (!mBleService.initialize()) {
                Toast.makeText(TrainingActivity.this, "not support Bluetooth", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBleService = null;
            mIsBind = false;
        }
    };

    private void doBindService() {
        Log.d(TAG,"doBindService " );
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

    private void setBleServiceListener() {
        mBleService.setOnConnectListener(new MultipleBleService.OnConnectionStateChangeListener() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                } else if (newState == BluetoothProfile.STATE_CONNECTING) {

                } else if (newState == BluetoothProfile.STATE_CONNECTED) {

                } else if (newState == BluetoothProfile.STATE_DISCONNECTING) {

                }
            }
        });
        mBleService.setOnServicesDiscoveredListener(new MultipleBleService.OnServicesDiscoveredListener() {
            @Override
            public void onServicesDiscovered(final BluetoothGatt gatt, int status){
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    Log.v(TAG, "Service discovery error: " + status);
                    return;
                }
                Log.v(TAG, "Service discovery completed");
                final String address = gatt.getDevice().getAddress();
                BluetoothGattService motionGattService = gatt.getService(ThingyUtils.THINGY_MOTION_SERVICE);
                if (motionGattService != null) {
                    /*mMotionConfigurationCharacteristic = motionGattService.getCharacteristic(ThingyUtils.THINGY_MOTION_CONFIGURATION_CHARACTERISTIC);
                    mTapCharacteristic = motionGattService.getCharacteristic(ThingyUtils.TAP_CHARACTERISTIC);
                    mOrientationCharacteristic = motionGattService.getCharacteristic(ThingyUtils.ORIENTATION_CHARACTERISTIC);
                    mQuaternionCharacteristic = motionGattService.getCharacteristic(ThingyUtils.QUATERNION_CHARACTERISTIC);
                    mPedometerCharacteristic = motionGattService.getCharacteristic(ThingyUtils.PEDOMETER_CHARACTERISTIC);*/
                    mRawDataCharacteristic = motionGattService.getCharacteristic(ThingyUtils.RAW_DATA_CHARACTERISTIC);
                    /*mEulerCharacteristic = motionGattService.getCharacteristic(ThingyUtils.EULER_CHARACTERISTIC);
                    mRotationMatrixCharacteristic = motionGattService.getCharacteristic(ThingyUtils.ROTATION_MATRIX_CHARACTERISTIC);
                    mHeadingCharacteristic = motionGattService.getCharacteristic(ThingyUtils.HEADING_CHARACTERISTIC);
                    mGravityVectorCharacteristic = motionGattService.getCharacteristic(ThingyUtils.GRAVITY_VECTOR_CHARACTERISTIC);*/
                    Log.v(TAG, "Reading motion config chars " + mRawDataCharacteristic);
                    if(null != mRawDataCharacteristic) {
                        readCharacteristic(address, mRawDataCharacteristic);
                    }
                }
                BluetoothGattService homegymService = gatt.getService(ThingyUtils.HOMEGYM_BASE_UUID);
                if(null != homegymService){
                    Log.d(TAG,"find homegym service");
                    mHomegymReadCharacteristic = homegymService.getCharacteristic(ThingyUtils.HOMEGYM_READ_UUID);
                    mHomegymWriteCharacteristic = homegymService.getCharacteristic(ThingyUtils.HOMEGYM_WRITE_UUID);
                    if(null != mHomegymReadCharacteristic){
                        Log.d(TAG,"find mHomegymReadCharacteristic");
                        readCharacteristic(address,mHomegymReadCharacteristic);
                    }
                    if(null != mHomegymWriteCharacteristic) {
                        Log.d(TAG,"find mHomegymWriteCharacteristic");
                        byte[] data = getParameter();
                        boolean result = mBleService.writeCharacteristic(address, ThingyUtils.HOMEGYM_BASE_UUID,
                                ThingyUtils.HOMEGYM_WRITE_UUID, data);
                        //mHomegymWriteCharacteristic.setValue(data);
                        //boolean result =  gatt.writeCharacteristic(mHomegymWriteCharacteristic);
                        Log.d(TAG,"writeCharacteristic result " + result);
                    }
                }
            }
        });
        mBleService.setOnDataAvailableListener(new MultipleBleService.OnDataAvailableListener() {
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                Log.d(TAG,"onCharacteristicRead");
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                Log.d(TAG,"some thing changed ");

                if(characteristic.equals(mHomegymReadCharacteristic)){
                    byte[] data = mHomegymReadCharacteristic.getValue();
                    logData(data);
                }
                if (characteristic.equals(mRawDataCharacteristic)) {
                    boolean isChanege = getDeviceValue(characteristic);
                    Log.d(TAG, "onCharacteristicChanged " + characteristic + "   " + (characteristic.equals(mRawDataCharacteristic))
                    + " isChange " + isChanege + " dumbbell " + mDumbbellExerciseCounter);
                    if(isChanege){
                        mHandler.sendEmptyMessage(UPDATE_COUNT);
                    }
                }

            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                Log.d(TAG,"onDescriptorRead");
            }
        });
    }

    private boolean getDeviceValue(BluetoothGattCharacteristic characteristic){
        final float accelerometerX = (float) (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0)) / (2 << 14);
        final float accelerometerY = (float) (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 2)) / (2 << 14);
        final float accelerometerZ = (float) (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 4)) / (2 << 14);

        boolean isChange = false;
        int count = mDumbbellExercise.exerciseCounting(accelerometerX, accelerometerY, accelerometerZ);
        if(count != mDumbbellExerciseCounter){
            mDumbbellExerciseCounter = count;
            isChange = true;
        }
        return isChange;

        /*final float gyroscopeX = (float) (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 6)) / (2 << 14);
        final float gyroscopeY = (float) (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 8)) / (2 << 14);
        final float gyroscopeZ = (float) (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 10)) / (2 << 14);

        final float compassZ = (float) (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 12)) / (2 << 14);
        final float compassX = (float) (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 14)) / (2 << 14);
        final float compassY = (float) (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 16)) / (2 << 14);*/
    }

    private byte[] getParameter(){
        byte[] data = new byte[5];
        logData(data);
        ThingyUtils.setValue(data, 0, 0x02, BluetoothGattCharacteristic.FORMAT_UINT8);
        ThingyUtils.setValue(data, 1, 0x01, BluetoothGattCharacteristic.FORMAT_UINT8);
        ThingyUtils.setValue(data, 2, 0x10, BluetoothGattCharacteristic.FORMAT_UINT8);
        ThingyUtils.setValue(data, 3, 0x00, BluetoothGattCharacteristic.FORMAT_UINT8);

        //byte crcValue = CrcUtils.calcCrc8(data, data.length -1);
        //Log.d(TAG,"getParameter crcValue " + crcValue);
        ThingyUtils.setValue(data, 4, 0x2F, BluetoothGattCharacteristic.FORMAT_UINT8);

        logData(data);
        return data;
    }

    private void logData(byte[] data){
        StringBuilder logTemp = new StringBuilder("");
        int length = data.length;
        int temp = 0;
        String string;
        for(int i = 0; i< length; i++){
            temp = data[i] & 0xFF;
            string = Integer.toHexString(temp);
            if(string.length() < 2){
                logTemp.append(0);
            }
            logTemp.append(string);
        }
        Log.d(TAG,"logData ~~~~  " + logTemp.toString());
    }
    private void readCharacteristic(String addr, BluetoothGattCharacteristic characteristic){
        final String address = addr;
        final BluetoothGattCharacteristic readCharacteristic = characteristic;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBleService.readCharacteristic(address, readCharacteristic);
            }
        }, 200);
        mBleService.setCharacteristicNotification(address,readCharacteristic,true);
    }

    private void discoveryServices(){
        if(null != mConnectedDevices) {
            int length = mConnectedDevices.size();
            Log.d(TAG,"discoveryServices " + length);
            for(int i = 0; i < length; i++){
                BluetoothDevice device = mConnectedDevices.get(i);
                mBleService.discoverServices(device.getAddress());
            }
        }

    }
}
