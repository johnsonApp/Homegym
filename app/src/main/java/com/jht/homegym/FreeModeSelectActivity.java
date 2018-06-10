package com.jht.homegym;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.jht.homegym.utils.Utils;

public class FreeModeSelectActivity extends AppCompatActivity{

    private final static String TAG = "FreeModeTrainingActivity";

    //private final static int TEXTUNSELECTCOLOR = R.color.colorTextUnSelect;
    //private final static int TEXTSELECTEDCOLOR = R.color.colorBlack;

    //private final static int UPPER = 0;
    //private final static int LOWER = 1;
    //private final static int CHEST = 2;
    //private final static int CORE = 3;

    //private ItemBody mUpperBody;
    //private ItemBody mLowerBody;
    //private ItemBody mChestBody;
    //private ItemBody mCoreBody;

    //private ArrayList<ItemBody> mList;

    private final String SELECT_MODE = "select_mode";

    private ImageView mBackBtn;
    private Button mStartBtn;
    private RelativeLayout mDumbbell;
    private RelativeLayout mHomegym;
    private RelativeLayout mRopeSkip;

    private int mIndex = Utils.HOMEGYM;

    private MyClickListener myClickListener;

    //private TextView mTrainingLabel;
    //private TextView mTrainingBody;
    //private Button mStartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_mode_select);
        /*
        mTrainingLabel = findViewById(R.id.training_label);
        mTrainingBody = findViewById(R.id.training_body);
        mStartButton = findViewById(R.id.start_training);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(-1 == mIndex){
                    Toast.makeText(FreeModeSelectActivity.this, "请选择要锻炼的身体部位！", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(FreeModeSelectActivity.this,CountdownActivity.class);
                    intent.putExtra("Mode",mIndex);
                    startActivity(intent);
                }
            }
        });
        initItemBody();*/
        mBackBtn = (ImageView) findViewById(R.id.back);
        mStartBtn = (Button) findViewById(R.id.start_training);
        mDumbbell = (RelativeLayout) findViewById(R.id.dumbbell);
        mHomegym = (RelativeLayout) findViewById(R.id.homegym);
        mRopeSkip = (RelativeLayout) findViewById(R.id.rope_skipping);
        myClickListener = new MyClickListener();
        mBackBtn.setOnClickListener(myClickListener);
        mStartBtn.setOnClickListener(myClickListener);
        mDumbbell.setOnClickListener(myClickListener);
        mHomegym.setOnClickListener(myClickListener);
        mRopeSkip.setOnClickListener(myClickListener);
    }

    class MyClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.back){
                FreeModeSelectActivity.this.finish();
            } else if (v.getId() == R.id.start_training) {
                Intent intent = new Intent();
                intent.setClass(FreeModeSelectActivity.this, CountdownActivity.class);
                intent.putExtra(SELECT_MODE, mIndex);
                startActivity(intent);
                FreeModeSelectActivity.this.finish();
            } else {
                switch (mIndex){
                    case Utils.DUMBBELL:
                        mDumbbell.setBackground(getResources().getDrawable(R.drawable.chosebtn_ssel));
                        break;
                    case Utils.HOMEGYM:
                        mHomegym.setBackground(getResources().getDrawable(R.drawable.chosebtn_bnor));
                        break;
                    case Utils.ROPE_SKIP:
                        mRopeSkip.setBackground(getResources().getDrawable(R.drawable.chosebtn_ssel));
                        break;
                }
                switch (v.getId()){
                    case R.id.dumbbell:
                        mDumbbell.setBackground(getResources().getDrawable(R.drawable.chosebtn_snor));
                        mIndex = Utils.DUMBBELL;
                        break;
                    case R.id.homegym:
                        mHomegym.setBackground(getResources().getDrawable(R.drawable.chosebtn_bsel));
                        mIndex = Utils.HOMEGYM;
                        break;
                    case R.id.rope_skipping:
                        mRopeSkip.setBackground(getResources().getDrawable(R.drawable.chosebtn_snor));
                        mIndex = Utils.ROPE_SKIP;
                        break;
                }
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        /*
        if(mIndex != -1){
            setTrainingLable();
        }
        */
    }

    /*
    private void setTrainingLable(){
        mTrainingLabel.setText(R.string.selected_someone);
        if(null != mList) {
            CharSequence temp = mList.get(mIndex).getBodyText();
            if(null != temp) {
                mTrainingBody.setText(temp);
            }
        }
    }
    */

    /*
    private void initItemBody() {
        mUpperBody = new ItemBody(findViewById(R.id.upper_body),this);
        mUpperBody.init(R.drawable.upper_body,R.string.upper_body);
        mLowerBody = new ItemBody(findViewById(R.id.lower_body),this);
        mLowerBody.init(R.drawable.lower_body,R.string.lower_body);
        mChestBody = new ItemBody(findViewById(R.id.chest_body),this);
        mChestBody.init(R.drawable.chest_body,R.string.chest_body);
        mCoreBody = new ItemBody(findViewById(R.id.core_body),this);
        mCoreBody.init(R.drawable.core_body,R.string.core_body);

        if(null == mList){
            mList = new ArrayList<>();
        }
        mList.add(UPPER,mUpperBody);
        mList.add(LOWER,mLowerBody);
        mList.add(CHEST,mChestBody);
        mList.add(CORE,mCoreBody);
    }
    */

    /*
    @Override
    public void onClick(View view){
        int which = 0;
        switch (view.getId()){
            case R.id.upper_body:
                which = UPPER;
                break;
            case R.id.lower_body:
                which = LOWER;
                break;
            case R.id.chest_body:
                which = CHEST;
                break;
            case R.id.core_body:
                which = CORE;
                break;
        }
        if(mIndex != which) {
            setItemSelected(which);
            mIndex = which;
            setTrainingLable();
        }
    }
    */

    /*
    private void setItemSelected(int which){
        for(int i = UPPER; i <= CORE; i++){
            if(i == which){
                mList.get(i).setIsSelected(true);
            }else {
                mList.get(i).setIsSelected(false);
            }
        }
    }*/

    /*
    private class ItemBody {
        private ImageView mbodyImage;
        private TextView mBodyText;
        private ImageView mSelectedImage;
        //private boolean mIsSeletcted = false;

        public ItemBody(View view, View.OnClickListener clickListener){
            mbodyImage = view.findViewById(R.id.body_image);
            mBodyText = view.findViewById(R.id.body_text);
            mSelectedImage = view.findViewById(R.id.item_selected);
            view.setOnClickListener(clickListener);
        }

        public void init(int bodyImage, int bodyText){
            setBodyImage(bodyImage);
            setBodyText(bodyText);
        }

        public CharSequence getBodyText(){
            if(null != mBodyText){
                return mBodyText.getText();
            }
            return null;
        }

        private void setBodyImage(int bodyImage){
            mbodyImage.setImageResource(bodyImage);
        }

        private void setBodyText(int bodyText){
            mBodyText.setText(bodyText);
        }

        public void setIsSelected(boolean isSelected){
            //Log.d("ItemBody","setIsSelected " + isSelected);
                //mIsSeletcted = isSelected;
                if (isSelected) {
                    mSelectedImage.setVisibility(View.VISIBLE);
                    setBodyTextColor(getResources().getColor(TEXTSELECTEDCOLOR));
                } else {
                    mSelectedImage.setVisibility(View.INVISIBLE);
                    setBodyTextColor(getResources().getColor(TEXTUNSELECTCOLOR));
                }
        }

        private void setBodyTextColor(int color){
            mBodyText.setTextColor(color);
        }
    }
    */
}
