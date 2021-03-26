package com.bixin.bluetooth.view.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.bixin.bluetooth.R;
import com.bixin.bluetooth.model.adapter.ViewPagerFragmentPagerAdapter;
import com.bixin.bluetooth.model.bean.HfpStatus;
import com.bixin.bluetooth.model.key.HomeKey;
import com.bixin.bluetooth.model.service.GocsdkService;
import com.bixin.bluetooth.view.fragment.CallPhoneFragment;
import com.bixin.bluetooth.view.fragment.MusicFragment;
import com.bixin.bluetooth.view.fragment.PhoneFragment;
import com.bixin.bluetooth.view.fragment.SettingsFragment;
import com.goodocom.gocsdk.IGocsdkService;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class BtHomeActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "BtHomeActivity";
    private ViewPager mViewPager;
    private FragmentManager fragmentManager;
    private ArrayList<Fragment> mFragments;
    private static IGocsdkService iGocsdkService;
    private Intent gocsdkService;
    private MyConn conn;
    private Context mContext;
    public static final int MSG_COMING = 4;
    public static final int MSG_OUTGING = 5;
    public static final int MSG_TALKING = 6;
    public static final int MSG_HANGUP = 7;
    public static final int MSG_DEVICENAME = 11;
    public static final int MSG_DEVICEPINCODE = 12;
    public static final int MSG_UPDATE_PHONEBOOK = 17;
    public static final int MSG_UPDATE_PHONEBOOK_DONE = 18;
    public static final int MSG_SET_MICPHONE_ON = 19;
    public static final int MSG_SET_MICPHONE_OFF = 20;
    public static final int MSG_UPDATE_INCOMING_CALLLOG = 25;
    public static final int MSG_UPDATE_CALLOUT_CALLLOG = 26;
    public static final int MSG_UPDATE_MISSED_CALLLOG = 27;
    public static final int MSG_UPDATE_CALLLOG_DONE = 28;
    public static final int MSG_CURRENT_CONNECT_DEVICE_NAME = 29;

    public static String mLocalName = null;
    public static String mPinCode = null;
    public static String currentDeviceName = "";
    private static Handler myHandler;
    private LinearLayout bt_phone;
    private LinearLayout bt_music;
    private LinearLayout bt_settings;
    private LinearLayout bt_dial;
    public static final String ACTION_SPEECH_TOOL_CMD = "com.bixin.speechrecognitiontool.action_cmd";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_home);
        myHandler = new MyHandler(this);
        getWindow().getDecorView().post(() -> myHandler.post(() -> {
            init();
            initView();
            //setHomeKey();
        }));
    }

    private void init() {
        this.mContext = this;
        fragmentManager = getSupportFragmentManager();
        mFragments = new ArrayList<>();
//        new Thread() {
//            public void run() {
//                Commands.initCommands();
//            }
//        }.start();
        initScreenReceiver();
        gocsdkService = new Intent(this, GocsdkService.class);
        gocsdkService.putExtra("foreground", false);
        conn = new MyConn();
        startService(gocsdkService);
        bindService(gocsdkService, conn, BIND_AUTO_CREATE);
    }

    private void initView() {
        mViewPager = findViewById(R.id.viewpager);
        mViewPager.setCurrentItem(0);
        mViewPager.setOffscreenPageLimit(4);
        bt_phone = findViewById(R.id.ll_bt_phone);
        bt_music = findViewById(R.id.ll_bt_music);
        bt_settings = findViewById(R.id.ll_bt_settings);
        bt_dial = findViewById(R.id.ll_bt_dial);
        bt_phone.setOnClickListener(this);
        bt_music.setOnClickListener(this);
        bt_settings.setOnClickListener(this);
        bt_dial.setOnClickListener(this);
        bt_phone.setSelected(true);
        setViewPager();
    }

    private void setViewPager() {
        mFragments.add(new PhoneFragment());
        mFragments.add(new CallPhoneFragment());
        mFragments.add(new MusicFragment());
        mFragments.add(new SettingsFragment());

        //设置viewPager的界面改变监听
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                cleanAllBtn();
                switch (position) {
                    case 0:
                        bt_phone.setSelected(true);
                        break;
                    case 1:
                        bt_dial.setSelected(true);
                        break;
                    case 2:
                        bt_music.setSelected(true);
                        break;
                    case 3:
                        bt_settings.setSelected(true);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        setViewPagerAdapter();
    }

    @SuppressLint("CheckResult")
    private void setViewPagerAdapter() {
        ViewPagerFragmentPagerAdapter adapter = new ViewPagerFragmentPagerAdapter(fragmentManager,
                ViewPagerFragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
                mFragments);
        mViewPager.setAdapter(adapter);
       /* Observable.create(new ObservableOnSubscribe<Object>() {

            @Override
            public void subscribe(@NonNull ObservableEmitter<Object> emitter) throws Exception {
                emitter.onNext("ok");
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {

                    }
                });*/
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        cleanAllBtn();
        if (viewId == R.id.ll_bt_phone) {
            bt_phone.setSelected(true);
            mViewPager.setCurrentItem(0);
        }
        if (viewId == R.id.ll_bt_music) {
            bt_music.setSelected(true);
            mViewPager.setCurrentItem(2);
        }
        if (viewId == R.id.ll_bt_settings) {
            bt_settings.setSelected(true);
            mViewPager.setCurrentItem(3);
        }
        if (viewId == R.id.ll_bt_dial) {
            bt_dial.setSelected(true);
            mViewPager.setCurrentItem(1);
        }
    }

    private void cleanAllBtn() {
        bt_music.setSelected(false);
        bt_phone.setSelected(false);
        bt_settings.setSelected(false);
        bt_dial.setSelected(false);
    }

    private void initScreenReceiver() {
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mFilter.addAction(Intent.ACTION_SCREEN_ON);
        mFilter.addAction(ACTION_SPEECH_TOOL_CMD);
        registerReceiver(mScreenReceiver, mFilter);
    }

    public static IGocsdkService getService() {
        return iGocsdkService;
    }

    private BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                Log.d("goc", "screen close!");
                try {
                    iGocsdkService.closeBlueTooth();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                Log.d("goc", "screen open!");
                try {
                    iGocsdkService.openBlueTooth();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else if (action.equals(ACTION_SPEECH_TOOL_CMD)) {
                String cmd = intent.getStringExtra("type");
                if (cmd.equals("bt_close")) {
                    finish();
                }
            }
        }
    };

    private class MyConn implements ServiceConnection {
        // 绑定成功之后 会调用该方法
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            iGocsdkService = IGocsdkService.Stub.asInterface(service);
//            try {
//                iGocsdkService.registerCallback(callback);
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }

            myHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        iGocsdkService.setAutoConnect();
                        iGocsdkService.inqueryHfpStatus();
                        iGocsdkService.inqueryA2dpStatus();
                        iGocsdkService.musicUnmute();
                        iGocsdkService.getLocalName();
                        iGocsdkService.getPinCode();
                        iGocsdkService.getCurrentDeviceName();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }, 500);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
        }
    }


    @SuppressLint("HandlerLeak")
    public static class MyHandler extends Handler {
        private BtHomeActivity mActivity;

        public MyHandler(BtHomeActivity mActivity) {
            WeakReference<BtHomeActivity> weakReference = new WeakReference<>(mActivity);
            this.mActivity = weakReference.get();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_HANGUP:
                    break;
                case MSG_COMING: {// 来电
                    String number = (String) msg.obj;
                    IncomingActivity.start(mActivity, HfpStatus.INCOMING, number);
                    break;
                }
                case MSG_TALKING: {// 通话中
                    String number = (String) msg.obj;
                    if ((!IncomingActivity.running) && (!CallActivity.running)) {
                        CallActivity.start(mActivity, HfpStatus.TALKING, number);
                    }
                    break;
                }
                case MSG_OUTGING:// 拨出
                    String number = (String) msg.obj;
                    if ((!IncomingActivity.running) && (!CallActivity.running)) {
                        CallActivity.start(mActivity, HfpStatus.CALLING, number);
                    }
                    break;
                case MSG_DEVICENAME:// 蓝牙设备名称
                    String name = (String) msg.obj;
                    mLocalName = name;
                    break;
                case MSG_DEVICEPINCODE:// 蓝牙设备的PIN码
                    String pinCode = (String) msg.obj;
                    mPinCode = pinCode;
                    Log.d(TAG, "handleMessage:mPinCode " + mPinCode);
                    break;
                case MSG_CURRENT_CONNECT_DEVICE_NAME:
                    currentDeviceName = (String) msg.obj;
                    break;
            }
        }
    }


    public static Handler getHandler() {
        return myHandler;
    }

    private HomeKey mHomeKey;

    private void setHomeKey() {
        mHomeKey = new HomeKey(this);
        mHomeKey.setOnHomePressedListener(new HomeKey.OnHomePressedListener() {

            @Override
            public void onHomePressed() {
                finish();
            }

            @Override
            public void onHomeLongPressed() {
                finish();
            }
        });
        mHomeKey.startWatch();
    }

    @Override
    protected void onDestroy() {
        //mHomeKey.stopWatch();
        Log.d("app", "MainActivity onDestroy");
        unregisterReceiver(mScreenReceiver);
        // 注销蓝牙回调
//        try {
//            iGocsdkService.unregisterCallback(callback);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
        // 解绑服务
        unbindService(conn);
        super.onDestroy();
    }
}
