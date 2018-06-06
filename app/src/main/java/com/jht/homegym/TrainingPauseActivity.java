package com.jht.homegym;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class TrainingPauseActivity extends Activity {
    private String TAG = "TrainingPauseActivity";
    private TextView mRestTime;

    private long startTime;
    private Timer mTimer = new Timer();
    private MyHandler mHandler = new MyHandler(this);
    class MyHandler extends Handler {
        WeakReference<TrainingPauseActivity> mActivity;

        MyHandler(TrainingPauseActivity activity){
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            TrainingPauseActivity activity = mActivity.get();
            if (activity != null){
                if (msg.what == 0){
                    mRestTime.setText(String.valueOf(msg.obj));
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_pause);
        Log.e(TAG, "onCreate");
        mHandler = new MyHandler(this);
        mRestTime = (TextView) findViewById(R.id.rest_time);
        Button holdTraining = (Button) findViewById(R.id.hold_train);
        Button finishTraining = (Button) findViewById(R.id.finish_train);
        holdTraining.setOnClickListener(listener);
        finishTraining.setOnClickListener(listener);

        startTime = SystemClock.elapsedRealtime();
        TimerTask mTimerTask = new TimerTask() {
            @Override
            public void run() {
                long time = (SystemClock.elapsedRealtime()- startTime) / 1000;
                String hh = new DecimalFormat("00").format(time / 3600);
                String mm = new DecimalFormat("00").format(time % 3600 / 60);
                String ss = new DecimalFormat("00").format(time % 60);
                String timeFormat = hh + ":" + mm + ":" + ss;
                mHandler.sendMessage(mHandler.obtainMessage(0, timeFormat));
            }
        };
        mTimer.schedule(mTimerTask, 1000, 1000);
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.hold_train:
                    setResult(TrainingActivity.RESULT_CONTINUE);
                    break;
                case R.id.finish_train:
                    setResult(TrainingActivity.RESULT_FINISH);
                    break;
            }
            finish();
        }
    };

    @Override
    public void finish() {
        mTimer.cancel();
        super.finish();
    }

    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
