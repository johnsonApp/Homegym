package com.jht.homegym;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.jht.homegym.ble.MultipleBleService;
import com.jht.homegym.dao.MyObjectBox;

import io.objectbox.BoxStore;
import io.objectbox.android.AndroidObjectBrowser;

public class HomegymApplication extends Application {

    private Intent mServiceIntent;
    private BoxStore mBoxStore;

    @Override
    public void onCreate() {
        super.onCreate();
        mBoxStore = MyObjectBox.builder().androidContext(this).build();
        if (BuildConfig.DEBUG) {
            new AndroidObjectBrowser(mBoxStore).start(this);
        }

        Log.d("App", "Using ObjectBox " + BoxStore.getVersion() + " (" + BoxStore.getVersionNative() + ")");

        mServiceIntent = new Intent(this, MultipleBleService.class);
        startService(mServiceIntent);
    }

    public void onTerminate(){
        super.onTerminate();
        if(null != mServiceIntent) {
            stopService(mServiceIntent);
        }
    }

    public BoxStore getBoxStore() {
        return mBoxStore;
    }
}
