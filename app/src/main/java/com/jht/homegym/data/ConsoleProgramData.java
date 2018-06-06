package com.jht.homegym.data;

import android.util.Log;


public class ConsoleProgramData {

    private static final String TAG = "ConsoleProgramData";

    private static final int PROGRAM_DATA_LENGTH = 11;
    private int mTimeMinute;
    private int mTimeSecond;
    private int mTimes;
    private int mCountDownTimer;
    private int mResistance;
    private int mProgramStatus;
    private int mProgramStage;

    public ConsoleProgramData() {
    }

    public ConsoleProgramData(byte[] data){
        getAllValueFromData(data);
    }

    public boolean getAllValueFromData(byte[] data){
        if(null == data || PROGRAM_DATA_LENGTH != data.length){
            Log.d(TAG,"ConsoleProgramData need valid data");
            return false;
        }
        byte[] temp = new byte[2];
        mTimeMinute = data[2] & 0xFF;
        mTimeSecond = data[3] & 0xFF;
        mTimes = bytesToInt(data,4,5);
        mCountDownTimer = data[6] & 0xFF;
        mResistance = data[7] & 0xFF;
        mProgramStatus = data[8] & 0xFF;
        mProgramStage = bytesToInt(data,9,10);
        return true;
    }

    public int bytesToInt(byte[] src, int start, int end) {
        int value;
        value = (src[start]&0xFF) | ((src[end] & 0xFF)<<8);
        return value;
    }

    public int getTimeMinute(){
        return mTimeMinute;
    }

    public int getTimeSecond(){
        return mTimeSecond;
    }

    public int getTimes(){
        return mTimes;
    }
    public int getCountDownTimer(){
        return mCountDownTimer;
    }

    public int getResistance(){
        return mResistance;
    }

    public int getProgramStatus(){
        return mProgramStatus;
    }

    public int getmProgramStage(){
        return mProgramStage;
    }
}
