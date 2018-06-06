package com.jht.homegym.data;

import android.util.Log;

public class AccessoryData {

    private static final String TAG = "AccessoryData";

    private static final int PROGRAM_DATA_LENGTH = 6;
    private int mTotalTimeMinute;
    private int mTotalTimeSecond;
    private int mTimes;

    public AccessoryData() {
    }

    public AccessoryData(byte[] data){
        getAllValueFromData(data);
    }

    public boolean getAllValueFromData(byte[] data){
        if(null == data || PROGRAM_DATA_LENGTH != data.length){
            Log.d(TAG,"ConsoleProgramData need valid data");
            return false;
        }
        byte[] temp = new byte[2];
        mTotalTimeMinute = data[2] & 0xFF;
        mTotalTimeMinute = data[3] & 0xFF;
        mTimes = bytesToInt(data,4,5);
        return true;
    }

    public int bytesToInt(byte[] src, int start, int end) {
        int value;
        value = (src[start]&0xFF) | ((src[end] & 0xFF)<<8);
        return value;
    }

    public int getTimeMinute(){
        return mTotalTimeMinute;
    }

    public int getTimeSecond(){
        return mTotalTimeSecond;
    }

    public int getTimes(){
        return mTimes;
    }
}
