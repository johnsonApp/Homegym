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
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jht.homegym.ble.MultipleBleService;
import com.jht.homegym.dao.App;
import com.jht.homegym.dao.FreeTraining;
import com.jht.homegym.utils.ThingyUtils;
import com.jht.homegym.algorithm.AccessoryExercise;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.objectbox.Box;
import io.objectbox.query.Query;

public class TrainingActivity extends Activity {

    private static final String  TAG = "TrainingActivity";

    public static final int SERVICE_BIND = 1;
    public static final int UPDATE_COUNT = 2;
    public static final int DUMBBELL = 3;
    public static final int ROPE_SKIP = 4;
    public static final int HOMEGYM = 5;
    public static final int TIME_CHANGE = 6;
    public static final int REQUEST = 7;
    public static final int RESULT_CONTINUE = 8;
    public static final int RESULT_FINISH = 9;

    private int mSelectIndex = -1;

    private TextView mCountDown;
    private RelativeLayout mDumbbellPart;
    private RelativeLayout mRopeSkippingPart;
    private ImageView mDumbbellPic;
    private ImageView mRopeSkippingPic;
    private TextView mDumbbellValue;
    private TextView mRopeSkippingValue;
    private TextView mTrainingTime;
    private TextView mResistanceValue;
    private SeekBar mResistanceBar;

    private long startTime;
    private Timer mTimer = new Timer();

    private Box<FreeTraining> mFreeTrainingBox;
    private Query<FreeTraining> mFreeTrainingQuery;

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
                case DUMBBELL:
                    //mCountDown.setText("0");
                    mRopeSkippingPart.setBackgroundColor(getResources().getColor(R.color.colorBlack));
                    mDumbbellPart.setBackgroundColor(getResources().getColor(R.color.colorSelectPart));
                    mRopeSkippingPic.setImageDrawable(getResources().getDrawable(R.drawable.training_ropeskipping_nor));
                    mDumbbellPic.setImageDrawable(getResources().getDrawable(R.drawable.training_dumbbell_sel));
                    mRopeSkippingValue.setTextColor(getResources().getColor(R.color.colorTextUnSelect));
                    mDumbbellValue.setTextColor(getResources().getColor(R.color.colorWhite));
                    break;
                case ROPE_SKIP:
                    //mCountDown.setText("0");
                    mRopeSkippingPart.setBackgroundColor(getResources().getColor(R.color.colorSelectPart));
                    mDumbbellPart.setBackgroundColor(getResources().getColor(R.color.colorBlack));
                    mRopeSkippingPic.setImageDrawable(getResources().getDrawable(R.drawable.training_ropeskipping_sel));
                    mDumbbellPic.setImageDrawable(getResources().getDrawable(R.drawable.training_dumbbell_nor));
                    mRopeSkippingValue.setTextColor(getResources().getColor(R.color.colorWhite));
                    mDumbbellValue.setTextColor(getResources().getColor(R.color.colorTextUnSelect));
                    break;
                case HOMEGYM:
                    mRopeSkippingPart.setBackgroundColor(getResources().getColor(R.color.colorBlack));
                    mDumbbellPart.setBackgroundColor(getResources().getColor(R.color.colorBlack));
                    mRopeSkippingPic.setImageDrawable(getResources().getDrawable(R.drawable.training_ropeskipping_nor));
                    mDumbbellPic.setImageDrawable(getResources().getDrawable(R.drawable.training_dumbbell_nor));
                    mRopeSkippingValue.setTextColor(getResources().getColor(R.color.colorTextUnSelect));
                    mDumbbellValue.setTextColor(getResources().getColor(R.color.colorTextUnSelect));
                    break;
                case TIME_CHANGE:
                    mTrainingTime.setText(String.valueOf(msg.obj));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
        setContentView(R.layout.activity_training);
        mSelectIndex = getIntent().getIntExtra("selectIndex", -1);
        Log.e(TAG,"selectIndex = " + mSelectIndex);

        mCountDown = (TextView) findViewById(R.id.count_down);
        mCountDown.setOnClickListener(listener);

        mDumbbellPart = (RelativeLayout) findViewById(R.id.dumbbell_part);
        mRopeSkippingPart = (RelativeLayout) findViewById(R.id.rope_skipping_part);
        mDumbbellPic = (ImageView) findViewById(R.id.dumbbell_pic);
        mRopeSkippingPic = (ImageView) findViewById(R.id.rope_skipping_pic);
        mDumbbellValue = (TextView) findViewById(R.id.dumbbell_value);
        mRopeSkippingValue = (TextView) findViewById(R.id.rope_skipping_value);
        mDumbbellPart.setOnClickListener(listener);
        mRopeSkippingPart.setOnClickListener(listener);
        if (mSelectIndex == 0){
            mHandler.sendEmptyMessage(DUMBBELL);
        } else if (mSelectIndex == 1){
            mHandler.sendEmptyMessage(HOMEGYM);
        } else if (mSelectIndex == 2){
            mHandler.sendEmptyMessage(ROPE_SKIP);
        }

        mTrainingTime = (TextView) findViewById(R.id.time_value);
        startTime = SystemClock.elapsedRealtime();
        TimerTask mTimerTask = new TimerTask() {
            @Override
            public void run() {
                long time = (SystemClock.elapsedRealtime()- startTime) / 1000;
                String hh = new DecimalFormat("00").format(time / 3600);
                String mm = new DecimalFormat("00").format(time % 3600 / 60);
                String ss = new DecimalFormat("00").format(time % 60);
                String timeFormat = hh + ":" + mm + ":" + ss;
                mHandler.sendMessage(mHandler.obtainMessage(TIME_CHANGE, timeFormat));
            }
        };
        mTimer.schedule(mTimerTask, 1000, 1000);
        mResistanceValue = (TextView) findViewById(R.id.weight_value);
        mResistanceBar = (SeekBar) findViewById(R.id.resistance_bar);
        mResistanceBar.setOnSeekBarChangeListener(resistanceListener);

        //ObjectBox manage the database
        mFreeTrainingBox = ((App) getApplication()).getBoxStore().boxFor(FreeTraining.class);
        mFreeTrainingQuery = mFreeTrainingBox.query().build();
        //List<FreeTraining> listFreeTraining = mFreeTrainingQuery.find();

        mDumbbellExercise = new AccessoryExercise();
        doBindService();
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.count_down:
                    mHandler.sendEmptyMessage(HOMEGYM);
                    break;
                case R.id.dumbbell_part:
                    mHandler.sendEmptyMessage(DUMBBELL);
                    break;
                case R.id.rope_skipping_part:
                    mHandler.sendEmptyMessage(ROPE_SKIP);
                    break;
            }
        }
    };

    SeekBar.OnSeekBarChangeListener resistanceListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mResistanceValue.setText(String.valueOf(progress));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

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
                        //boolean result = mBleService.writeCharacteristic(address, ThingyUtils.HOMEGYM_BASE_UUID,
                        //        ThingyUtils.HOMEGYM_WRITE_UUID, data);
                        mHomegymWriteCharacteristic.setValue(data);
                        boolean result =  gatt.writeCharacteristic(mHomegymWriteCharacteristic);
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Intent intent = new Intent();
        intent.setClass(TrainingActivity.this, TrainingPauseActivity.class);
        startActivityForResult(intent, REQUEST);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode){
            case RESULT_CONTINUE:

                break;
            case RESULT_FINISH:

                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void finish() {
        mTimer.cancel();
        super.finish();
    }
}
