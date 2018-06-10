package com.jht.homegym;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class CountdownActivity extends Activity {
    private ImageView mBack;
    private TextView mCountDown;
    private int mCurValue = 5;
    private TimerTask mTimerTask;
    private Timer mTimer = new Timer();
    private MyHandler mHandler = new MyHandler(this);
    private Intent mIntent;

    class MyHandler extends Handler{
        WeakReference<CountdownActivity> mActivity;

        MyHandler(CountdownActivity activity){
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            CountdownActivity activity = mActivity.get();
            if (activity != null){
                if (msg.what == 0){
                    activity.mCountDown.setText(String.valueOf(--activity.mCurValue));
                    if (activity.mCurValue == 0){
                        CountdownActivity.this.finish();
                        //倒计时结束进入锻炼页面
                        mIntent.setClass(CountdownActivity.this, TrainingActivity.class);
                        startActivity(mIntent);
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);
        mIntent = getIntent();
        mBack = (ImageView) findViewById(R.id.back);
        mBack.setOnClickListener(listener);
        mCountDown = (TextView) findViewById(R.id.count_down);
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(0);
            }
        };
        mTimer.schedule(mTimerTask, 1000, 1000);
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.back){
                CountdownActivity.this.finish();
            }
        }
    };

    @Override
    public void finish() {
        mTimer.cancel();
        mTimer.purge();
        super.finish();
    }
}
