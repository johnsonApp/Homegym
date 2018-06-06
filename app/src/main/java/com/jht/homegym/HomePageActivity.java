package com.jht.homegym;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

import java.lang.ref.WeakReference;

public class HomePageActivity extends AppCompatActivity implements View.OnClickListener,
        MainPageFragment.OnFragmentInteractionListener{

    private final static String Tag = "HomePageActivity";

    private final static int MAIN_PAGE = 1;
    private final static int USERCENTER_PAGE = 2;
    private final static int CUSTOMMADE_PAGE = 3;
    private final static int CHALLENGE_PAGE = 4;
    private final static int CLUB_PAGE = 5;

    private TextView mMainPage;
    private TextView mUserCenter;
    private TextView mCustomMade;
    private TextView mChallenge;
    private TextView mClub;

    private int mCurrentPage = 0;
    private Fragment mCurrentFragment = null;
    private MainPageFragment mMainPageFragment;
    private UserCenterFragment mUserCenterFragment;
    private ChallengeFragment mChallengeFragment;
    private TrainingClassFragment mTrainingClassFragment;

    BluetoothAdapter mBluetoothAdapter;

    private TextView mConnectStatus;
    private MyHandler myHandler = new MyHandler(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_user_profile);
        /*BluetoothManager bluetoothManager =
                (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter =bluetoothManager.getAdapter();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this,"this device is not support ble", Toast.LENGTH_SHORT).show();

        }*/
        mMainPage = findViewById(R.id.main_page);
        mMainPage.setOnClickListener(this);
        mUserCenter = findViewById(R.id.use_center);
        mUserCenter.setOnClickListener(this);
        mCustomMade = findViewById(R.id.custom_made);
        mCustomMade.setOnClickListener(this);
        mChallenge = findViewById(R.id.challenge);
        mChallenge.setOnClickListener(this);
        mClub = findViewById(R.id.club);
        mClub.setOnClickListener(this);

        mMainPageFragment = new MainPageFragment();
        mUserCenterFragment = new UserCenterFragment();
        mChallengeFragment = new ChallengeFragment();
        mTrainingClassFragment = new TrainingClassFragment();
        //addFragmentToContainer(mMainPageFragment,"main_page");
        switchFragment(mMainPageFragment);
        mCurrentPage = MAIN_PAGE;

        mConnectStatus = (TextView) findViewById(R.id.connect_status);
        mConnectStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (String.valueOf(mConnectStatus.getText()).equals("UNCONNECT")){
                    myHandler.sendEmptyMessage(0);
                }else if (String.valueOf(mConnectStatus.getText()).equals("CONNECT")){
                    myHandler.sendEmptyMessage(1);
                }
            }
        });
    }

    @Override
    public void onClick(View view){
        int newPage = 0;
        switch (view.getId()){
            case R.id.main_page:
                if(mCurrentPage != MAIN_PAGE){
                    switchFragment(mMainPageFragment);
                    newPage = MAIN_PAGE;
                }
                break;
            case R.id.use_center:
                if(mCurrentPage != USERCENTER_PAGE) {
                    switchFragment(mUserCenterFragment);
                    newPage = USERCENTER_PAGE;
                }
                break;
            case R.id.custom_made:
                if(mCurrentPage != CUSTOMMADE_PAGE){
                    switchFragment(mTrainingClassFragment);
                    newPage = CUSTOMMADE_PAGE;
                }
                break;
            case R.id.challenge:
                if(mCurrentPage != CHALLENGE_PAGE) {
                    switchFragment(mChallengeFragment);
                    newPage = CHALLENGE_PAGE;
                }
                break;
            case R.id.club:{
                if(mCurrentPage != CLUB_PAGE) {
                    newPage = CLUB_PAGE;
                }
            }
        }
        if(newPage != 0){
            setTabBackground(mCurrentPage,false);
            setTabBackground(newPage,true);
            mCurrentPage = newPage;
        }
    }

    private void setTabBackground(int page,boolean isFocus){
        int backgourdColor;
        if(isFocus) {
            backgourdColor = getResources().getColor(R.color.colorButtonSelected);
        }else {
            backgourdColor = getResources().getColor(R.color.colorBlack);
        }
        switch (page){
            case MAIN_PAGE:
                mMainPage.setBackgroundColor(backgourdColor);
                break;
            case USERCENTER_PAGE:
                mUserCenter.setBackgroundColor(backgourdColor);
                break;
            case CUSTOMMADE_PAGE:
                mCustomMade.setBackgroundColor(backgourdColor);
                break;
            case CHALLENGE_PAGE:
                mChallenge.setBackgroundColor(backgourdColor);
                break;
            case CLUB_PAGE:
                mClub.setBackgroundColor(backgourdColor);
                break;
        }
    }

    /*private void addFragmentToContainer() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        //transaction.add(fragment,tag);
        transaction.add(R.id.fragment_container,mMainPageFragment);
        transaction.commit();
    }*/

    private void switchFragment(Fragment targetFragment) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        if(!targetFragment.isAdded()) {
            if (mCurrentFragment != null) {
                transaction.hide(mCurrentFragment);
            }
            transaction.add(R.id.fragment_container,targetFragment,targetFragment.getClass().getName());
        }else {
            transaction
                    .hide(mCurrentFragment)
                    .show(targetFragment);
        }
        mCurrentFragment = targetFragment;
        transaction.commit();
    }

    public void onFragmentInteraction(Uri uri){

    }

    class MyHandler extends Handler {
        WeakReference<HomePageActivity> mActivity;

        MyHandler(HomePageActivity activity){
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            HomePageActivity activity = mActivity.get();
            if (activity != null){
                if (msg.what == 0){
                    mConnectStatus.setText("CONNECT");
                    Drawable drawable = getResources().getDrawable(R.drawable.ble_connect, null);
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                    mConnectStatus.setCompoundDrawablesRelative(drawable, null, null, null);
                } else if (msg.what == 1){
                    mConnectStatus.setText("UNCONNECT");
                    Drawable drawable = getResources().getDrawable(R.drawable.ble_unconnect, null);
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                    mConnectStatus.setCompoundDrawablesRelative(drawable, null, null, null);
                }
            }
        }
    }
}
