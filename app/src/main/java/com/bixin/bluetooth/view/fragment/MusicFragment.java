package com.bixin.bluetooth.view.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bixin.bluetooth.R;
import com.bixin.bluetooth.model.bean.A2dpStatus;
import com.bixin.bluetooth.model.bean.BxBTApp;
import com.bixin.bluetooth.model.bean.Commands;
import com.bixin.bluetooth.model.event.A2dpStatusEvent;
import com.bixin.bluetooth.model.event.MusicInfoEvent;
import com.bixin.bluetooth.model.event.MusicPosEvent;
import com.bixin.bluetooth.model.event.PlayStatusEvent;
import com.bixin.bluetooth.model.service.GocsdkCallbackImp;
import com.bixin.bluetooth.model.service.GocsdkService;
import com.bixin.bluetooth.model.tools.ToastTool;
import com.bixin.bluetooth.view.activity.BtHomeActivity;
import com.goodocom.gocsdk.IGocsdkService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;

public class MusicFragment extends Fragment implements View.OnClickListener {
    private AudioManager audioManager;
    private ImageView ivPlay;
    private ImageView ivNext;
    private ImageView ivPrevious;
    private TextView tvMusicLyric;
    private TextView tvMusicArtist;
    private TextView tvMusicSinger;
    private static Handler mHandler;
    private String musicName = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        audioManager = (AudioManager) BxBTApp.getInstance().getSystemService(Context.AUDIO_SERVICE);
        mHandler = new MyHandler(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mHandler.sendEmptyMessageAtTime(102, 800);
        View view = inflater.inflate(R.layout.fragment_music, container, false);
        // 用来接收消息的
        EventBus.getDefault().register(this);
        initView(view);
        return view;
    }

    private void initView(View view) {
        ivPlay = view.findViewById(R.id.iv_play);
        ivNext = view.findViewById(R.id.iv_next);
        ivPrevious = view.findViewById(R.id.iv_previous);

        tvMusicLyric = view.findViewById(R.id.tv_music_lyric);
        tvMusicArtist = view.findViewById(R.id.tv_music_artist);
        tvMusicSinger = view.findViewById(R.id.tv_music_singer);
        ivPlay.setOnClickListener(this);
        ivPrevious.setOnClickListener(this);
        ivNext.setOnClickListener(this);
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PlayStatusEvent status) {
        if (!status.playing) {
            Log.d("app", "PlayStatusEvent is pause!");
            ivPlay.setSelected(false);
        } else {
            Log.d("app", "PlayStatusEvent is play!");
            ivPlay.setSelected(true);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MusicPosEvent pos) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(A2dpStatusEvent event) {
        if (event.status <= A2dpStatus.CONNECTED) {
            ivPlay.setSelected(false);
            Log.d("app", "A2dpStatusEvent is pause!");
//            iv_pause.setVisibility(View.VISIBLE);
//            iv_play.setVisibility(View.GONE);
        } else {
            ivPlay.setSelected(true);
            Log.d("app", "A2dpStatusEvent is play!");

//            iv_pause.setVisibility(View.VISIBLE);
//            iv_play.setVisibility(View.GONE);
        }
    }

    // 接收音乐信息方法
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MusicInfoEvent info) {
//        updateMusic(info);
        Message message = Message.obtain();
        message.what = 101;
        message.obj = info;
        mHandler.sendMessage(message);
    }

    @Override
    public void onClick(View v) {
        if (GocsdkCallbackImp.hfpStatus < 3) {
            ToastTool.showToast(R.string.please_connect_bt);
            return;
        }

        int viewID = v.getId();
        if (BtHomeActivity.getService() != null) {
            try {
                if (viewID == R.id.iv_previous) {
                    BtHomeActivity.getService().musicPrevious();
                }
                if (viewID == R.id.iv_next) {
                    BtHomeActivity.getService().musicNext();
                }
                if (viewID == R.id.iv_play) {
                    ivPlay.setSelected(!ivPlay.isSelected());
                    BtHomeActivity.getService().musicPlayOrPause();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateMusic(MusicInfoEvent info) {
        Log.d("TAG", "updateMusic: " + info.toString());
        if (musicName == null || !musicName.equals(info.album)) {
            musicName = info.album;
//            String[] text = info.artist.trim().split("—");
//            tvMusicArtist.setText(text[0]);
//            tvMusicSinger.setText(text[1]);
            tvMusicArtist.setText(musicName);
            tvMusicSinger.setText(info.artist);
        }
        tvMusicLyric.setText(info.name);
    }

    @SuppressLint("HandlerLeak")
    public static class MyHandler extends Handler {
        private MusicFragment fragment;

        public MyHandler(MusicFragment itemFragment) {
            WeakReference<MusicFragment> mWeakReference = new WeakReference<>(itemFragment);
            this.fragment = mWeakReference.get();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 101://update music
                    fragment.updateMusic((MusicInfoEvent) msg.obj);
                    break;
                case 102:
                    fragment.initMusic();
                    break;
            }
        }
    }

    private void initMusic() {
        /**
         * 该页面只要加载了，先判断蓝牙是否连接， 如果连接就发送Handler消息给GocsdkService，
         * 让它发送AT命令字节，调用回调接口的方法，发送音乐信息
         */
        if (GocsdkCallbackImp.hfpStatus > 0) {
            try {
                IGocsdkService service = BtHomeActivity.getService();
                if (service != null) {
                    service.getMusicInfo();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

}