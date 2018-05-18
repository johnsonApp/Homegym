package com.jht.homegym;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class TrainingActivity extends Activity {
    private ImageView mBodyImage;
    private LinearLayout mSelectLayout;
    private int[] mBtnId = {R.id.button1, R.id.button2, R.id.button3, R.id.button4};
    private Button[] mButtons = new Button[mBtnId.length];
    private int mCurPart = 0;//当前选中的身体部分

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
}
