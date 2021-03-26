package com.bixin.bluetooth.model.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;


import androidx.annotation.RequiresApi;

import com.bixin.bluetooth.model.bean.BxBTApp;
import com.bixin.bluetooth.model.bean.Commands;
import com.bixin.bluetooth.view.fragment.PhoneFragment;
import com.bixin.bluetooth.view.fragment.SettingsFragment;
import com.bixin.bluetooth.model.bean.HfpStatus;
import com.bixin.bluetooth.model.bean.PhoneBook;
import com.bixin.bluetooth.model.event.A2dpStatusEvent;
import com.bixin.bluetooth.model.event.CurrentNumberEvent;
import com.bixin.bluetooth.model.event.HfpStatusEvent;
import com.bixin.bluetooth.model.event.MusicInfoEvent;
import com.bixin.bluetooth.model.event.MusicPosEvent;
import com.bixin.bluetooth.model.event.PlayStatusEvent;
import com.bixin.bluetooth.view.activity.BtHomeActivity;
import com.goodocom.gocsdk.IGocsdkCallback;
import com.goodocom.gocsdk.IGocsdkService;

import org.greenrobot.eventbus.EventBus;


public class GocsdkCallbackImp extends IGocsdkCallback.Stub {
    private static final String TAG="GocsdkCallbackImp";
    public static String number = "";
    public static int hfpStatus = 1;
    public static int a2dpStatus = 1;

    private AudioManager mAudioManager;
    private int mAudioFocus = AudioManager.AUDIOFOCUS_NONE;
    private GocsdkService gocsdkService;
    public GocsdkCallbackImp(GocsdkService gocsdkService){
        this.gocsdkService = gocsdkService;
        mAudioManager = (AudioManager) BxBTApp.getInstance().getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void onHfpConnected() throws RemoteException {
        Log.d(TAG, "onHfpConnected: ");
//        Handler handler2 = FragmentPairedList.getHandler();
//        if (handler2 != null) {
//            handler2.sendEmptyMessage(FragmentPairedList.MSG_CONNECT_SUCCESS);
//        }
        Handler handler = SettingsFragment.getHandler();
        if (handler != null) {
            handler.sendEmptyMessage(SettingsFragment.MSG_CONNECT_SUCCESS);
        }
        Handler handler3 = PhoneFragment.getHandler();
        if (handler3 != null) {
            handler3.sendEmptyMessage(PhoneFragment.MSG_DEVICE_CONNECT);
        }
//        Handler handler4 = FragmentCallog.getHandler();
//        if (handler4 != null) {
//            handler4.sendEmptyMessage(FragmentCallog.MSG_DEVICE_CONNECTED);
//        }
        GocsdkCallbackImp.hfpStatus = 3;
    }

    @Override
    public void onHfpDisconnected() throws RemoteException {
        Handler handler = SettingsFragment.getHandler();
        if (handler != null) {
            handler.sendEmptyMessage(SettingsFragment.MSG_CONNECT_FAILE);
        }
//		Handler handler1 = FragmentCallog.getHandler();
//		if(handler1!=null){
//			handler1.sendEmptyMessage(FragmentCallog.MSG_DEVICE_DISCONNECTED);
//		}
		Handler handler2 = SettingsFragment.getHandler();
		if(handler2!=null){
			handler2.sendEmptyMessage(SettingsFragment.MSG_CONNECT_FAILE);
		}
        Handler handler3 = PhoneFragment.getHandler();
        if (handler3 != null) {
            handler3.sendEmptyMessage(PhoneFragment.MSG_CONNECT_FAILE);
        }
        GocsdkCallbackImp.hfpStatus = 1;
    }

    @Override
    public void onCallSucceed(String number) throws RemoteException {
        Log.d(TAG, "GocsdkCallbackImp onCallSucceed" + number);
        Handler handler = BtHomeActivity.getHandler();
        if (handler != null) {
            handler.sendMessage(handler.obtainMessage(BtHomeActivity.MSG_OUTGING, number));
        }
        GocsdkCallbackImp.hfpStatus = 4;
    }

    @Override
    public void onIncoming(String number) throws RemoteException {

        Handler handler1 = BtHomeActivity.getHandler();
        handler1.sendMessage(handler1.obtainMessage(BtHomeActivity.MSG_COMING,
                number));
        GocsdkCallbackImp.number = number;
        GocsdkCallbackImp.hfpStatus = 5;

    }

    @Override
    public void onHangUp() throws RemoteException {
        abandonAudioFocus();
        Handler handler1 = BtHomeActivity.getHandler();
        if (handler1 != null) {
            handler1.sendEmptyMessage(BtHomeActivity.MSG_HANGUP);
        }
    }

    @Override
    public void onTalking(String str) throws RemoteException {
        Handler handler = BtHomeActivity.getHandler();
        if (handler == null) {
            return;
        }
        handler.sendMessage(handler.obtainMessage(BtHomeActivity.MSG_TALKING, str));
        GocsdkCallbackImp.hfpStatus = 6;
    }

    @Override
    public void onRingStart() throws RemoteException {
    }

    @Override
    public void onRingStop() throws RemoteException {
    }

    @Override
    public void onHfpLocal() throws RemoteException {
    }

    @Override
    public void onHfpRemote() throws RemoteException {
    }

    @Override
    public void onInPairMode() throws RemoteException {
    }

    @Override
    public void onExitPairMode() throws RemoteException {
    }

    @Override
    public void onInitSucceed() throws RemoteException {

    }

    @Override
    public void onMusicPlaying() throws RemoteException {
        Log.d(TAG, "callback play status event true");
        requestAudioFocus();
        EventBus.getDefault().postSticky(new PlayStatusEvent(true));
    }

    @Override
    public void onMusicStopped() throws RemoteException {
        Log.d(TAG, "callback play status event false");
        abandonAudioFocus();
        EventBus.getDefault().postSticky(new PlayStatusEvent(false));
    }

    @Override
    public void onAutoConnectAccept(String autoStatus)
            throws RemoteException {
        Handler handler = SettingsFragment.getHandler();
        if (handler == null) {
            return;
        }
        Message msg = Message.obtain();
        msg.what = SettingsFragment.MSG_AUTO_STATUS;
        msg.obj = autoStatus;
        handler.sendMessage(msg);
    }

    @Override
    public void onCurrentAddr(String addr) throws RemoteException {
//        Handler handler2 = FragmentPairedList.getHandler();
//        if (handler2 != null) {
//            Message msg = Message.obtain();
//            msg.what = FragmentPairedList.MSG_CONNECT_ADDRESS;
//            msg.obj = addr;
//            handler2.sendMessage(msg);
//        }
    }

    @Override
    public void onCurrentName(String name) throws RemoteException {
        Handler handler = BtHomeActivity.getHandler();

        if (handler != null) {
            Message msg = Message.obtain();
            msg.what = BtHomeActivity.MSG_CURRENT_CONNECT_DEVICE_NAME;
            msg.obj = name;
            handler.sendMessage(msg);
        }
    }

    // 1:未连接 3:已连接 4：电话拨出 5：电话打入 6：通话中
    /*
     * 0~初始化 1~待机状态 2~连接中 3~连接成功 4~电话拨出 5~电话打入 6~通话中
     */
    @Override
    public void onHfpStatus(int status) throws RemoteException {
        int prevStatus = GocsdkCallbackImp.hfpStatus;
        Log.d(TAG, "onHfpStatus: status " + status);
        GocsdkCallbackImp.hfpStatus = status;

        Handler handler = SettingsFragment.getHandler();
        if (handler != null) {
            Message msg = Message.obtain();
            msg.what = SettingsFragment.MSG_HFP_STATUS;
            msg.obj = status;
            handler.sendMessage(msg);
        }
        Handler handler2 = PhoneFragment.getHandler();
        if (handler2 != null) {
            Message msg = Message.obtain();
            msg.what = PhoneFragment.MSG_HFP_STATUS;
            msg.obj = status;
            handler2.sendMessage(msg);
        }

        if (prevStatus <= HfpStatus.CONNECTED) {
            Handler mainHandler = BtHomeActivity.getHandler();
            if (mainHandler != null) {
                if (status == HfpStatus.CALLING) {
                    Log.d(TAG,"HfpStatus.CALLING");
                    requestAudioFocus();
                    mainHandler.sendMessage(mainHandler.obtainMessage(BtHomeActivity.MSG_OUTGING, ""));
                } else if (status == HfpStatus.INCOMING) {
                    Log.d(TAG,"HfpStatus.INCOMING");
                    requestAudioFocus();
                    mainHandler.sendMessage(mainHandler.obtainMessage(BtHomeActivity.MSG_COMING, ""));
                } else if (status == HfpStatus.TALKING) {
                    mainHandler.sendMessage(mainHandler.obtainMessage(BtHomeActivity.MSG_TALKING, ""));
                }
            }
        }

        EventBus.getDefault().postSticky(new HfpStatusEvent(status));
    }

    @Override
    public void onAvStatus(int status) throws RemoteException {
        a2dpStatus = status;
        EventBus.getDefault().post(new A2dpStatusEvent(status));
    }

    @Override
    public void onVersionDate(String version) throws RemoteException {
    }

    @Override
    public void onCurrentDeviceName(String name) throws RemoteException {
        Handler handler = BtHomeActivity.getHandler();
        if (handler != null) {
            Message msg = Message.obtain();
            msg.obj = name;
            msg.what = BtHomeActivity.MSG_DEVICENAME;
            handler.sendMessage(msg);
        }
        Handler handler1 = SettingsFragment.getHandler();
        if (handler1 != null) {
            Message msg = Message.obtain();
            msg.what = SettingsFragment.MSG_DEVICE_NAME;
            msg.obj = name;
            handler1.sendMessage(msg);
        }
//		Handler handler2 = FragmentSearch.getHandler();
//		if(handler2!=null){
//			Message msg = Message.obtain();
//			msg.what = FragmentSearch.MSG_DEVICE_NAME;
//			msg.obj = name;
//			handler2.sendMessage(msg);
//		}
    }

    @Override
    public void onCurrentPinCode(String code) throws RemoteException {
        Handler handler = BtHomeActivity.getHandler();

        if (handler != null) {
            Message msg = Message.obtain();
            msg.obj = code;
            msg.what = BtHomeActivity.MSG_DEVICEPINCODE;
            handler.sendMessage(msg);
        }
        Handler handler1 = SettingsFragment.getHandler();
        if (handler1 != null) {
            Message msg = Message.obtain();
            msg.what = SettingsFragment.MSG_PIN_CODE;
            Log.d(TAG, "onCurrentPinCode: code "+code);
            msg.obj = code;
            handler1.sendMessage(msg);
        }
//		Handler handler2 = FragmentSearch.getHandler();
//		if(handler2!=null){
//			Message msg = Message.obtain();
//			msg.what = FragmentSearch.MSG_PIN_CODE;
//			msg.obj = code;
//			handler2.sendMessage(msg);
//		}
    }

    @Override
    public void onA2dpConnected() throws RemoteException {
    }

    //配对列表
    @Override
    public void onCurrentAndPairList(int index, String name, String addr)
            throws RemoteException {
//        Handler handler = FragmentPairedList.getHandler();
//        if (handler == null) {
//            return;
//        }
//        BlueToothPairedInfo info = new BlueToothPairedInfo();
//        info.index = index;
//        info.name = name;
//        info.address = addr;
//        Message msg = Message.obtain();
//        msg.obj = info;
//        msg.what = FragmentPairedList.MSG_PAIRED_DEVICE;
//        handler.sendMessage(msg);
    }

    @Override
    public void onA2dpDisconnected() throws RemoteException {
    }

    @Override
    public void onPhoneBook(String name, String number) throws RemoteException {
        Handler handler = PhoneFragment.getHandler();
        if (handler == null) {
            return;
        }
        Message msg = Message.obtain();
        msg.what = PhoneFragment.MSG_PHONE_BOOK;
        PhoneBook phonebook = new PhoneBook();
        phonebook.setName(name);
        phonebook.setNumber(number);
        msg.obj = phonebook;
        handler.sendMessage(msg);
    }

    @Override
    public void onPhoneBookDone() throws RemoteException {
        Log.d(TAG, "onPhoneBookDone: ");
        Handler mainActivityHandler = BtHomeActivity.getHandler();
        if (mainActivityHandler == null) {
            Log.d(TAG, "onPhoneBookDone:mainActivityHandler == null ");
        } else {
            mainActivityHandler
                    .sendEmptyMessage(BtHomeActivity.MSG_UPDATE_PHONEBOOK_DONE);
        }
        Handler handler = PhoneFragment.getHandler();
        if (handler == null) {
            Log.d(TAG, "onPhoneBookDone:PhoneFragment == null ");
            return;
        }
        Message msg = Message.obtain();
        msg.what = PhoneFragment.MSG_PHONE_BOOK_DONE;
        handler.sendMessage(msg);
    }

    @Override
    public void onSimBook(String name, String number) throws RemoteException {

    }

    @Override
    public void onSimDone() throws RemoteException {

    }

    @Override
    public void onCalllog(int type, String name, String number)
            throws RemoteException {
//		Handler handler = FragmentCallog.getHandler();
//		if (handler == null) {
//			return;
//		}
//		CallLogInfo info = new CallLogInfo();
//		info.number = number;
//		info.type = type;
//		info.name = name;
//		Message msg = Message.obtain();
//		msg.obj = info;
//		msg.what = FragmentCallog.MSG_CALLLOG;
//		handler.sendMessage(msg);
    }

    @Override
    public void onCalllogDone() throws RemoteException {
//		Handler mainHandler = MainActivity.getHandler();
//		mainHandler.sendEmptyMessage(MainActivity.MSG_UPDATE_CALLLOG_DONE);
//
//		Handler handler = FragmentCallog.getHandler();
//		if (handler == null) {
//			return;
//		}
//		Message msg = Message.obtain();
//		msg.what = FragmentCallog.MSG_CALLLOG_DONE;
//		handler.sendMessage(msg);
    }

    @Override
    public void onDiscovery(String type, String name, String addr) throws RemoteException {
//		Handler handler = FragmentSearch.getHandler();
//		Message msg = Message.obtain();
//		msg.what = FragmentSearch.MSG_SEARCHE_DEVICE;
//		BlueToothInfo info = new BlueToothInfo();
//		info.name = name;
//		info.address = addr;
//		msg.obj = info;
//		if (handler == null) {
//			return;
//		}
//		handler.sendMessage(msg);
    }

    @Override
    public void onDiscoveryDone() throws RemoteException {
//		Handler handler = FragmentSearch.getHandler();
//		if (handler == null) {
//			return;
//		}
//		handler.sendEmptyMessage(FragmentSearch.MSG_SEARCHE_DEVICE_DONE);
    }

    @Override
    public void onLocalAddress(String addr) throws RemoteException {
    }

    //得到拨出或者通话中的号码
    @Override
    public void onOutGoingOrTalkingNumber(String number) throws RemoteException {
        EventBus.getDefault().postSticky(new CurrentNumberEvent(number));
    }

    @Override
    public void onConnecting() throws RemoteException {
    }

    @Override
    public void onSppData(int index, String data) throws RemoteException {
    }

    @Override
    public void onSppConnect(int index) throws RemoteException {
    }

    @Override
    public void onSppDisconnect(int index) throws RemoteException {
    }

    @Override
    public void onSppStatus(int status) throws RemoteException {
    }

    @Override
    public void onOppReceivedFile(String path) throws RemoteException {
    }

    @Override
    public void onOppPushSuccess() throws RemoteException {
    }

    @Override
    public void onOppPushFailed() throws RemoteException {
    }

    @Override
    public void onHidConnected() throws RemoteException {
    }

    @Override
    public void onHidDisconnected() throws RemoteException {
    }

    @Override
    public void onHidStatus(int status) throws RemoteException {
    }

    @Override
    public void onMusicInfo(String name, String artist, String album, int duration, int pos,
                            int total) throws RemoteException {
        EventBus.getDefault().post(
                new MusicInfoEvent(name, artist, album, duration, pos, total));
    }

    @Override
    public void onMusicPos(int current, int total) throws RemoteException {
        EventBus.getDefault().post(new MusicPosEvent(current, total));
    }

    @Override
    public void onPanConnect() throws RemoteException {
    }

    @Override
    public void onPanDisconnect() throws RemoteException {
    }

    @Override
    public void onPanStatus(int status) throws RemoteException {

    }

    @Override
    public void onVoiceConnected() throws RemoteException {
    }

    @Override
    public void onVoiceDisconnected() throws RemoteException {

    }

    @Override
    public void onProfileEnbled(boolean[] enabled) throws RemoteException {
    }

    @Override
    public void onMessageInfo(String content_order, String read_status,
                              String time, String name, String num, String title)
            throws RemoteException {/*
		EventBus.getDefault().post(
				new MessageListEvent(content_order,
						read_status.equals("1") ? true : false, time, name,
						num, title));
	*/
    }

    @Override
    public void onMessageContent(String content) throws RemoteException {
        //EventBus.getDefault().post(new MessageTextEvent(content));
    }

    @Override
    public void onPairedState(int state) throws RemoteException {
        Log.d(TAG, "" + state);
    }

    AudioFocusRequest focusRequest;
    @SuppressLint("NewApi")
    private int requestAudioFocus() {
        // Bluetooth A2DP may carry Music, Audio Books, Navigation, or other sounds so mark content
        // type unknown.
        Log.d(TAG, "requestAudioFocus");
        AudioAttributes streamAttributes =
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                        .build();
        // Bluetooth ducking is handled at the native layer so tell the Audio Manger to notify the
        // focus change listener via .setWillPauseWhenDucked().
        focusRequest =
                new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(streamAttributes)
                        .setWillPauseWhenDucked(true)
                        .setOnAudioFocusChangeListener(mAudioFocusListener)
                        .build();
         int focusRequestStatus = mAudioManager.requestAudioFocus(focusRequest);
        // If the request is granted begin streaming immediately and schedule an upgrade.
        if (focusRequestStatus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mAudioFocus = AudioManager.AUDIOFOCUS_GAIN;
        }
        return focusRequestStatus;
    }

    private AudioManager.OnAudioFocusChangeListener mAudioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            Log.d(TAG, "onAudioFocusChangeListener focuschange " + focusChange);
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    Log.d(TAG,"Commands.PLAY_PAUSE_MUSIC");
                    gocsdkService.sendMsg(GocsdkService.MSG_STOP_MUSIC);
                    abandonAudioFocus();
                    //GocsdkService.getInstance().write(Commands.PAUSE_MUSIC);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    break;
            }
        }
    };

    private void abandonAudioFocus() {
        Log.d(TAG, "abandonAudioFocus");
//        if (focusRequest != null) {
//            mAudioManager.abandonAudioFocusRequest(focusRequest);
//        }
        mAudioManager.abandonAudioFocus(mAudioFocusListener);
        mAudioFocus = AudioManager.AUDIOFOCUS_LOSS;
    }
}
