package com.jht.homegym;

import android.app.Application;
import android.content.Intent;

import com.jht.homegym.ble.MultipleBleService;

public class HomegymApplication extends Application {

    private Intent mServiceIntent;
    @Override
    public void onCreate() {
        super.onCreate();
        mServiceIntent = new Intent(this, MultipleBleService.class);
        startService(mServiceIntent);
    }

    public void onTerminate(){
        super.onTerminate();
        if(null != mServiceIntent) {
            stopService(mServiceIntent);
        }
    }
}
