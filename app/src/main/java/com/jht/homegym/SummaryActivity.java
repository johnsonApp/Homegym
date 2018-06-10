package com.jht.homegym;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jht.homegym.dao.FreeTraining;
import com.jht.homegym.dao.FreeTraining_;

import java.text.DecimalFormat;

import io.objectbox.Box;
import io.objectbox.query.Query;

public class SummaryActivity extends Activity {
    private ImageView mHomePage;
    private ImageView mUserCenter;
    private TextView mTrainingTime;
    private TextView mTrainingNum;
    private TextView mTrainingResistance;

    private Box<FreeTraining> mFreeTrainingBox;
    private Query<FreeTraining> mFreeTrainingQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        mHomePage = (ImageView) findViewById(R.id.home_pic);
        mTrainingTime = (TextView) findViewById(R.id.bottom1_txt2);
        mTrainingNum = (TextView) findViewById(R.id.bottom2_txt2);
        mTrainingResistance = (TextView) findViewById(R.id.bottom3_txt1);
        mHomePage.setOnClickListener(listener);
        //ObjectBox manage the database
        long queryID = getIntent().getLongExtra("TRAINING_ID", 1L);
        mFreeTrainingBox = ((HomegymApplication) getApplication()).getBoxStore().boxFor(FreeTraining.class);
        mFreeTrainingQuery = mFreeTrainingBox.query().equal(FreeTraining_.id, queryID).build();
        FreeTraining freeTraining = mFreeTrainingQuery.findFirst();
        mTrainingTime.setText(timeReversal(freeTraining.getTotalTime()));
        mTrainingNum.setText(String.valueOf(freeTraining.getTotalNum()));
        mTrainingResistance.setText(String.valueOf(freeTraining.getLevel()));
    }

    private String timeReversal(int time){
        String hh = new DecimalFormat("00").format(time / 3600);
        String mm = new DecimalFormat("00").format(time % 3600 / 60);
        String ss = new DecimalFormat("00").format(time % 60);
        return hh + ":" + mm + ":" + ss;
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };
}
