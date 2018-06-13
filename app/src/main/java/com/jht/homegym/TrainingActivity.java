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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.jht.homegym.ble.BLECommand;
import com.jht.homegym.ble.MultipleBleService;

import com.jht.homegym.algorithm.AccessoryExercise;
import com.jht.homegym.dao.FreeTraining;
import com.jht.homegym.data.AccessoryData;
import com.jht.homegym.data.ConsoleProgramData;
import com.jht.homegym.utils.CrcUtils;
import com.jht.homegym.utils.Utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.objectbox.Box;
import io.objectbox.query.Query;

public class TrainingActivity extends Activity {

    private static final String  TAG = "TrainingActivity";

    private final String SELECT_MODE = "select_mode";

    private static final int SERVICE_BIND = 1;
    private static final int UPDATE_HOMEGYM_COUNT = 2;
    private static final int UPDATE_ACCESSORY_COUNT = 3;
    private static final int MSG_DUMBBELL = 4;
    private static final int MSG_ROPE_SKIP = 5;
    private static final int TIME_CHANGE = 6;
    private static final int REST_TIME_CHANGE = 7;
    private static final int MSG_REQUEST_ACCESSORY_MODE = 8;
    private static final int MSG_SWITCH_PAUSE = 9;
    private static final int MSG_SWITCH_TRAINING = 10;
    private static final int MSG_SWITCH_STOP = 11;

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
    private TextView mTotalTrainingValue;

    private TextView mTrainingRestTime;
    private Button mHoldTraining;
    private Button mFinishTraining;

    private RelativeLayout mLayoutTraining;
    private LinearLayout mLayoutTrainPause;

    private String mHomegymAddress = null;
    private String mAccessoryAddress = null;

    private long mStartTime = 0L;
    private long mRestTime = 0L;
    private int mTotalTime = 0;
    private Timer mTimer = new Timer();
    private TimerTask mTimerTask;

    private Box<FreeTraining> mFreeTrainingBox;
    //private Query<FreeTraining> mFreeTrainingQuery;

    private MultipleBleService mBleService;
    private List<BluetoothDevice> mConnectedDevices;

    private AccessoryExercise mDumbbellExercise;
    private AccessoryExercise mRopeExercise;
    private int mDumbbellExerciseCounter = 0;
    private int mRopeSkipExerciseCounter = 0;
    private int mHomegymExerciseCounter = 0;
    private int mTotalExerciseCounter = 0;

    private BluetoothGattCharacteristic mAccessoryReadCharacteristic;
    private BluetoothGattCharacteristic mAccessoryWriteCharacteristic;
    private BluetoothGattCharacteristic mAccessorySensorCharacteristic;

    private BluetoothGattCharacteristic mHomegymReadCharacteristic;
    private BluetoothGattCharacteristic mHomegymWriteCharacteristic;

    private int mSelectIndex = 0;
    private int mAccessoryMode = BLECommand.INVAILD_DATA;

    private int mCurrentStatus = BLECommand.PROGRAM_STATUS_STOP;

    private Handler mHandler = new Handler() {
        boolean result = false;
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SERVICE_BIND:
                    setBleServiceListener();
                    mConnectedDevices = mBleService.getConnectDevices();
                    discoveryServices();
                    break;
                case UPDATE_HOMEGYM_COUNT:
                    mCountDown.setText(String.valueOf(mHomegymExerciseCounter));
                    break;
                case UPDATE_ACCESSORY_COUNT:
                    if(Utils.DUMBBELL == mAccessoryMode) {
                        mDumbbellValue.setText(String.valueOf(mDumbbellExerciseCounter));
                    }else if(Utils.ROPE_SKIP == mAccessoryMode) {
                        mRopeSkippingValue.setText(String.valueOf(mRopeSkipExerciseCounter));
                    }
                    //mRopeSkippingValue.setText(String.valueOf(mRopeSkipExerciseCounter));
                    break;
                case MSG_DUMBBELL:
                    mDumbbellPart.setBackgroundColor(getResources().getColor(R.color.colorSelectPart));
                    mRopeSkippingPart.setBackgroundColor(getResources().getColor(R.color.colorBlack));
                    mDumbbellPic.setImageDrawable(getResources().getDrawable(R.drawable.training_dumbbell_sel));
                    mRopeSkippingPic.setImageDrawable(getResources().getDrawable(R.drawable.training_ropeskipping_nor));
                    mDumbbellValue.setTextColor(getResources().getColor(R.color.colorWhite));
                    mRopeSkippingValue.setTextColor(getResources().getColor(R.color.colorTextUnSelect));
                    if(Utils.DUMBBELL != mAccessoryMode) {
                        setAccessoryMode(Utils.DUMBBELL);
                        mAccessoryMode = Utils.DUMBBELL;
                    }
                    break;
                case MSG_ROPE_SKIP:
                    mDumbbellPart.setBackgroundColor(getResources().getColor(R.color.colorBlack));
                    mRopeSkippingPart.setBackgroundColor(getResources().getColor(R.color.colorSelectPart));
                    mDumbbellPic.setImageDrawable(getResources().getDrawable(R.drawable.training_dumbbell_nor));
                    mRopeSkippingPic.setImageDrawable(getResources().getDrawable(R.drawable.training_ropeskipping_sel));
                    mDumbbellValue.setTextColor(getResources().getColor(R.color.colorTextUnSelect));
                    mRopeSkippingValue.setTextColor(getResources().getColor(R.color.colorWhite));
                    if(Utils.ROPE_SKIP != mAccessoryMode) {
                        setAccessoryMode(Utils.ROPE_SKIP);
                        mAccessoryMode = Utils.ROPE_SKIP;
                    }
                    break;
                case MSG_REQUEST_ACCESSORY_MODE:
                    result = getAccessoryMode();
                    Log.d(TAG,"MSG_REQUEST_ACCESSORY_MODE result " + result);
                    break;
                case TIME_CHANGE:
                    mTrainingTime.setText(String.valueOf(msg.obj));
                    break;
                case REST_TIME_CHANGE:
                    mTrainingRestTime.setText(String.valueOf(msg.obj));
                    break;
                case MSG_SWITCH_PAUSE:
                    switchToPause();
                    break;
                case MSG_SWITCH_TRAINING:
                    switchToTraining();
                    break;
                case MSG_SWITCH_STOP:
                    finishTraining();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
        setContentView(R.layout.activity_training);
        mCountDown = (TextView) findViewById(R.id.count_down);
        mDumbbellPart = (RelativeLayout) findViewById(R.id.dumbbell_part);
        mRopeSkippingPart = (RelativeLayout) findViewById(R.id.rope_skipping_part);
        mDumbbellPic = (ImageView) findViewById(R.id.dumbbell_pic);
        mRopeSkippingPic = (ImageView) findViewById(R.id.rope_skipping_pic);
        mDumbbellValue = (TextView) findViewById(R.id.dumbbell_value);
        mRopeSkippingValue = (TextView) findViewById(R.id.rope_skipping_value);
        mTotalTrainingValue = (TextView) findViewById(R.id.count_value);
        mTrainingTime = (TextView) findViewById(R.id.time_value);
        mResistanceValue = (TextView) findViewById(R.id.weight_value);
        mResistanceBar = (SeekBar) findViewById(R.id.resistance_bar);
        mDumbbellPart.setOnClickListener(listener);
        mRopeSkippingPart.setOnClickListener(listener);
        mCountDown.addTextChangedListener(textWatcher);
        mDumbbellValue.addTextChangedListener(textWatcher);
        mRopeSkippingValue.addTextChangedListener(textWatcher);
        mResistanceBar.setOnSeekBarChangeListener(resistanceListener);

        mTrainingRestTime = (TextView) findViewById(R.id.rest_time);
        mHoldTraining = (Button) findViewById(R.id.hold_train);
        mFinishTraining = (Button) findViewById(R.id.finish_train);
        mHoldTraining.setOnClickListener(listener);
        mFinishTraining.setOnClickListener(listener);

        mLayoutTraining = (RelativeLayout) findViewById(R.id.layout_training);
        mLayoutTrainPause = (LinearLayout) findViewById(R.id.layout_training_pause);

        mSelectIndex = getIntent().getIntExtra(SELECT_MODE, Utils.HOMEGYM);
        Log.e(TAG,"selectIndex = " + mSelectIndex);

        mStartTime = SystemClock.elapsedRealtime();
        /*mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.sendMessage(mHandler.obtainMessage(TIME_CHANGE, timeReversal(SystemClock.elapsedRealtime()- mStartTime + mTotalTime)));
            }
        };
        mTimer.schedule(mTimerTask, 1000, 1000);*/

        //ObjectBox manage the database
        mFreeTrainingBox = ((HomegymApplication) getApplication()).getBoxStore().boxFor(FreeTraining.class);
        //mFreeTrainingQuery = mFreeTrainingBox.query().build();
        //List<FreeTraining> listFreeTraining = mFreeTrainingQuery.find();

        mDumbbellExercise = new AccessoryExercise(Utils.DUMBBELL);
        mRopeExercise = new AccessoryExercise(Utils.ROPE_SKIP);
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.dumbbell_part:
                    mHandler.sendEmptyMessage(MSG_DUMBBELL);
                    break;
                case R.id.rope_skipping_part:
                    mHandler.sendEmptyMessage(MSG_ROPE_SKIP);
                    break;
                case R.id.hold_train:
                    switchToTraining();
                    break;
                case R.id.finish_train:
                    finishTraining();
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
            setHomegyResistance(seekBar.getProgress());
        }
    };

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mTotalExerciseCounter = mDumbbellExerciseCounter + mRopeSkipExerciseCounter + mHomegymExerciseCounter;
            mTotalTrainingValue.setText(String.valueOf(mTotalExerciseCounter));
        }
    };

    public void onStart(){
        super.onStart();
        doBindService();
    }

    public void onStop(){
        super.onStop();
        setProgramNoticeEnable(false);
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
        }
    };

    private void doBindService() {
        Log.d(TAG,"doBindService " );
        Intent serviceIntent = new Intent(this, MultipleBleService.class);
        bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void doUnBindService() {
        unbindService(mServiceConnection);
        mBleService = null;

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
                BluetoothGattService accessoryGattService = gatt.getService(Utils.ACCESSORY_BASE_UUID);
                if (accessoryGattService != null) {
                    mAccessoryAddress = address;
                    Log.d(TAG,"find accessory service");
                    mAccessoryWriteCharacteristic = accessoryGattService.getCharacteristic(Utils.ACCESSORY_WRITE_UUID);
                    if(null != mAccessoryWriteCharacteristic) {
                        switch (mSelectIndex) {
                            case Utils.DUMBBELL:
                                mHandler.sendEmptyMessage(MSG_DUMBBELL);
                                break;
                            case Utils.ROPE_SKIP:
                                mHandler.sendEmptyMessage(MSG_ROPE_SKIP);
                                break;
                            case Utils.HOMEGYM:
                                mHandler.sendEmptyMessage(MSG_REQUEST_ACCESSORY_MODE);
                                break;
                        }
                        Log.d(TAG,"find accessory  write characteristic ");
                    }

                    mAccessoryReadCharacteristic = accessoryGattService.getCharacteristic(Utils.ACCESSORY_READ_UUID);
                    if(null != mAccessoryReadCharacteristic) {
                        readCharacteristic(address, mAccessoryReadCharacteristic,0,200);
                        Log.d(TAG,"find accessory  read characteristic");
                    }

                    mAccessorySensorCharacteristic = accessoryGattService.getCharacteristic(Utils.ACCESSORY_SENSOR_UUID);
                    if(null != mAccessorySensorCharacteristic) {
                        readCharacteristic(address, mAccessorySensorCharacteristic,600,800);
                        Log.d(TAG, "find accessory  sensor characteristic");
                    }

                }
                BluetoothGattService homegymService = gatt.getService(Utils.HOMEGYM_BASE_UUID);
                if(null != homegymService){
                    mHomegymAddress = address;
                    Log.d(TAG,"find homegym service");
                    mHomegymReadCharacteristic = homegymService.getCharacteristic(Utils.HOMEGYM_READ_UUID);
                    mHomegymWriteCharacteristic = homegymService.getCharacteristic(Utils.HOMEGYM_WRITE_UUID);
                    if(null != mHomegymReadCharacteristic){
                        Log.d(TAG,"find mHomegymReadCharacteristic");
                        readCharacteristic(address,mHomegymReadCharacteristic,0,300);
                    }
                    if(null != mHomegymWriteCharacteristic) {
                        Log.d(TAG,"find mHomegymWriteCharacteristic");
                        /*byte[] data = BLECommand.getParameter(BLECommand.GET_PARAMETER_BATTERY);
                        byte[] data = BLECommand.getParameter(BLECommand.GET_PARAMETER_PROGRAM_DATA);
                        logData(data);
                        boolean result = mBleService.writeCharacteristic(address, Utils.HOMEGYM_BASE_UUID,
                        Utils.HOMEGYM_WRITE_UUID, data);*/

                        boolean result = setProgramNoticeEnable(true);
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
                //Log.d(TAG,"some thing changed");
                //logData(characteristic.getValue());
                /*byte[] dataTemp = {02,0x0b,0x11,0x08,0x00,(byte)0x46,0x36,0x00,0x00,0x00,0x04,0x02,0x00,0x00};
                byte[] data = new byte[dataTemp.length + 1];
                int crc = CrcUtils.calcCrc8(dataTemp);
                for(int i = 0; i < dataTemp.length; i++) {
                    data[i] = dataTemp[i];
                }
                data[dataTemp.length] = (byte) (crc & 0xff);*/
                byte[] data = characteristic.getValue();
                int mode = BLECommand.getPacketMode(data);
                if(BLECommand.COMMAND_REPLY_PARAMETER == mode){
                    byte[] dataSource = BLECommand.unpacketReplyParameter(data);
                    if(null != dataSource && dataSource.length > 1){
                        updateReplyMessage(dataSource[0], dataSource);
                    }
                }
                if(characteristic.equals(mHomegymReadCharacteristic)){
                    logData(data);
                    Log.d(TAG,"onCharacteristicChanged mode " + mode);
                }
                if (characteristic.equals(mAccessoryReadCharacteristic)) {
                    Log.d(TAG,"get mAccessoryReadCharacteristic");
                    logData(data);
                }
                if(characteristic.equals(mAccessorySensorCharacteristic)) {
                    Log.d(TAG,"get mAccessorySensorCharacteristic mode " + mode);

                    logData(data);
                    if(BLECommand.COMMAND_ACCESSORY_SENSOR == mode){
                        Log.d(TAG,"getAccessoryValue ");
                            int sensorMode = BLECommand.getSensorDataMode(data);
                            float[] sensorData = BLECommand.unpacketSensorData(data);
                            boolean isChanege = getAccessoryValue(sensorData);
                            if(isChanege){
                                mHandler.sendEmptyMessage(UPDATE_ACCESSORY_COUNT);
                            }
                    }
                }

            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                Log.d(TAG,"onDescriptorRead");
            }
        });
    }

    private boolean getAccessoryValue(float[] data){
        if(null == data || data.length < 3){
            Log.d(TAG,"need vaild sensor data");
            return false;
        }
        final float accelerometerX = data[0];
        final float accelerometerY = data[1];
        final float accelerometerZ = data[2];

        boolean isChange = false;
        int count = 0;

        Log.d(TAG,"getAccessoryValue count " + count + " mDumbbellExerciseCounter " + mDumbbellExerciseCounter);
        if(Utils.DUMBBELL == mAccessoryMode) {
            count = mDumbbellExercise.exerciseCounting(accelerometerX, accelerometerY, accelerometerZ);
            if (count != mDumbbellExerciseCounter) {
                mDumbbellExerciseCounter = count;
                isChange = true;
            }
        }else if (Utils.ROPE_SKIP == mAccessoryMode) {
            count = mRopeExercise.exerciseCounting(accelerometerX, accelerometerY, accelerometerZ);
            if (count != mRopeSkipExerciseCounter) {
                mRopeSkipExerciseCounter = count;
                isChange = true;
            }
        }
        return isChange;
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
    private void readCharacteristic(String addr, BluetoothGattCharacteristic characteristic,long notifyDealy, long readDealy){
        final String address = addr;
        final BluetoothGattCharacteristic readCharacteristic = characteristic;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(null != mBleService) {
                    mBleService.setCharacteristicNotification(address, readCharacteristic, true);
                }
            }
        }, notifyDealy);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(null != mBleService) {
                    mBleService.readCharacteristic(address, readCharacteristic);
                }
            }
        }, readDealy);
    }

    private boolean setProgramNoticeEnable(boolean enable){
        boolean result = false;
        if(null != mBleService) {
            byte[] data = BLECommand.setProgramNoticeEnable(enable);
            Log.d(TAG,"setProgramNoticeEnable " + enable);
            /*if(null != mAccessoryAddress){
                mBleService.writeCharacteristic(mAccessoryAddress, Utils.ACCESSORY_BASE_UUID,
                        Utils.ACCESSORY_WRITE_UUID, data);
            }*/
            if(null != mHomegymAddress) {
                result = mBleService.writeCharacteristic(mHomegymAddress, Utils.HOMEGYM_BASE_UUID,
                        Utils.HOMEGYM_WRITE_UUID, data);
            }
        }
        return result;

    }
    private boolean getAccessoryMode() {
        if(null != mBleService){
            byte[] data = BLECommand.getParameter(BLECommand.GET_PARAMETER_ACCESSORY_MODE);
            Log.d(TAG,"getAccessoryMode ");
            logData(data);
            return mBleService.writeCharacteristic(mAccessoryAddress, Utils.ACCESSORY_BASE_UUID,
                    Utils.ACCESSORY_WRITE_UUID, data);
        }
        return false;
    }

    private boolean setAccessoryMode(int mode) {
        if(null != mBleService){
            byte[] data = BLECommand.setParameter(BLECommand.SET_PARAMETER_ACCESSORY_MODE,mode);
            Log.d(TAG,"setAccessoryMode ");
            logData(data);
            return mBleService.writeCharacteristic(mAccessoryAddress, Utils.ACCESSORY_BASE_UUID,
                    Utils.ACCESSORY_WRITE_UUID, data);
        }
        return false;
    }

    private boolean  setHomegyResistance(int levle){
        if(null != mBleService){
            byte[] data = BLECommand.setParameter(BLECommand.SET_PARAMETER_RESISTANCE,levle);
            return mBleService.writeCharacteristic(mHomegymAddress, Utils.HOMEGYM_BASE_UUID,
                    Utils.HOMEGYM_WRITE_UUID, data);
        }
        return false;
    }
    private void discoveryServices(){
        if(null != mConnectedDevices) {
            int length = mConnectedDevices.size();
            Log.d(TAG,"discoveryServices " + length);
            for(int i = 0; i < length; i++){
                BluetoothDevice device = mConnectedDevices.get(i);
                Log.d(TAG,"discoveryServices address " + device.getAddress());
                boolean result = mBleService.discoverServices(device.getAddress());
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mLayoutTraining.getVisibility() == View.VISIBLE){
            if (event.getAction() == MotionEvent.ACTION_UP){
                //Intent intent = new Intent();
                //intent.setClass(TrainingActivity.this, TrainingPauseActivity.class);
                //startActivityForResult(intent, REQUEST);
                switchToPause();
            }
        }
        return true;
    }

    public void onBackPressed() {
        super.onBackPressed();
        finishTraining();
    }

    private void switchToPause(){
        mTimer.cancel();
        mTimer.purge();
        mLayoutTraining.setVisibility(View.GONE);
        mLayoutTrainPause.setVisibility(View.VISIBLE);
        mRestTime = SystemClock.elapsedRealtime();
        mHandler.sendMessage(mHandler.obtainMessage(REST_TIME_CHANGE, "00:00:00"));
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.sendMessage(mHandler.obtainMessage(REST_TIME_CHANGE, timeReversal(SystemClock.elapsedRealtime()- mRestTime)));
            }
        };
        mTimer.schedule(mTimerTask, 1000, 1000);
        //mTotalTime = mRestTime - mStartTime + mTotalTime;
    }

    private void switchToTraining(){
        mTimer.cancel();
        mTimer.purge();
        mLayoutTrainPause.setVisibility(View.GONE);
        mLayoutTraining.setVisibility(View.VISIBLE);
        mStartTime = SystemClock.elapsedRealtime();
        /*mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.sendMessage(mHandler.obtainMessage(TIME_CHANGE, timeReversal(SystemClock.elapsedRealtime()- mStartTime + mTotalTime)));
            }
        };
        mTimer.schedule(mTimerTask, 1000, 1000);*/
    }

    private void finishTraining(){
        mTimer.cancel();
        mTimer.purge();
        FreeTraining freeTraining = new FreeTraining();
        freeTraining.setUserId(1);
        freeTraining.setCurTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));
        freeTraining.setTotalTime((int)(mTotalTime));
        freeTraining.setDumbbellNum(mDumbbellExerciseCounter);
        freeTraining.setSkipRopeNum(mRopeSkipExerciseCounter);
        freeTraining.setPullRopeNum(mHomegymExerciseCounter);
        freeTraining.setTotalNum(mTotalExerciseCounter);
        freeTraining.setLevel(mResistanceBar.getProgress());
        long id = mFreeTrainingBox.put(freeTraining);
        Intent intent = new Intent(TrainingActivity.this, SummaryActivity.class);
        intent.putExtra("TRAINING_ID", id);
        startActivity(intent);
        finish();
    }

    private String timeReversal(long time){
        time = time / 1000;
        String hh = new DecimalFormat("00").format(time / 3600);
        String mm = new DecimalFormat("00").format(time % 3600 / 60);
        String ss = new DecimalFormat("00").format(time % 60);
        return hh + ":" + mm + ":" + ss;
    }

    private String timeReversal(int min, int sec){
        String hh = new DecimalFormat("00").format(min / 60);
        String mm = new DecimalFormat("00").format(min % 60);
        String ss = new DecimalFormat("00").format(sec);
        return hh + ":" + mm + ":" + ss;
    }

    @Override
    public void finish() {
        mTimer.cancel();
        super.finish();
    }

    private void updateReplyMessage(int mode, byte[] data){
        if(BLECommand.INVAILD_DATA == mode || null == data){
            return;
        }
        Log.d(TAG,"updateReplyMessage mode " + mode + " length  " + data.length);
        byte [] unpacketResult = null;
        switch (mode){
            case BLECommand.GET_PARAMETER_VERSION:
                break;
            case BLECommand.GET_PARAMETER_BATTERY:
                break;
            case BLECommand.GET_PARAMETER_ACCESSORY_MODE:
                if(BLECommand.REPLY_PARAMETER_ACCESSORY_MODE_LENGTH == data.length){
                    int accessory = data[1];
                    if(Utils.DUMBBELL == accessory) {
                        mAccessoryMode = accessory;
                        mHandler.sendEmptyMessage(MSG_DUMBBELL);
                    }else if(Utils.ROPE_SKIP == accessory) {
                        mAccessoryMode = accessory;
                        mHandler.sendEmptyMessage(MSG_ROPE_SKIP);
                    }
                }
                break;
            case BLECommand.GET_PARAMETER_PROGRAM_DATA:
                int programMode = data[1];
                if(BLECommand.PROGRAM_MODE_CONCOLE == programMode){
                    logData(data);
                    ConsoleProgramData programData = new ConsoleProgramData(data);
                    if(null != programData) {
                        Log.d(TAG,"updateReplyMessage Resistance " +  programData.getResistance());
                        mResistanceBar.setProgress(programData.getResistance());
                        mHomegymExerciseCounter = programData.getTimes();
                        Log.d(TAG,"updateReplyMessage Times = " +  mHomegymExerciseCounter);
                        mHandler.sendEmptyMessage(UPDATE_HOMEGYM_COUNT);
                        int status = programData.getProgramStatus();
                        if(mCurrentStatus != status){
                            switch(status) {
                                case BLECommand.PROGRAM_STATUS_STOP:
                                    mHandler.sendEmptyMessage(MSG_SWITCH_STOP);
                                    break;
                                case BLECommand.PROGRAM_STATUS_PAUSE:
                                    mHandler.sendEmptyMessage(MSG_SWITCH_PAUSE);
                                    break;
                                case BLECommand.PROGRAM_STATUS_START:
                                    mHandler.sendEmptyMessage(MSG_SWITCH_TRAINING);
                                    break;
                            }
                            mCurrentStatus = status;
                        }
                        int min = programData.getTimeMinute();
                        int sec = programData.getTimeSecond();
                        int total = (min * 60 + sec);
                        if(mTotalTime != total) {
                            mHandler.sendMessage(mHandler.obtainMessage(TIME_CHANGE, timeReversal(min, sec)));
                            mTotalTime = total;
                        }
                    }
                }else if(BLECommand.PROGRAM_MODE_ACCESSORY == programMode) {
                    AccessoryData accessoryData = new AccessoryData(data);
                }
                break;

        }
    }
}
