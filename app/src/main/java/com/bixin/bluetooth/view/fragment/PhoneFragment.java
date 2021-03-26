package com.bixin.bluetooth.view.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bixin.bluetooth.R;
import com.bixin.bluetooth.model.adapter.ContactsRecyclerViewAdapter;
import com.bixin.bluetooth.model.bean.PhoneBook;
import com.bixin.bluetooth.model.db.GocDatabase;
import com.bixin.bluetooth.model.listener.RecyclerViewOnItemListener;
import com.bixin.bluetooth.model.service.GocsdkCallbackImp;
import com.bixin.bluetooth.model.tools.SharePreferenceTool;
import com.bixin.bluetooth.model.tools.ToastTool;
import com.bixin.bluetooth.view.activity.BtHomeActivity;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.trello.rxlifecycle2.android.FragmentEvent;
import com.trello.rxlifecycle2.components.support.RxFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class PhoneFragment extends RxFragment implements View.OnClickListener, RecyclerViewOnItemListener {
    private Context mContext;
    private final static String TAG = "PhoneFragment";
    public final static int MSG_PHONE_BOOK = 1;// 更新联系人
    public final static int MSG_PHONE_BOOK_DONE = 2;// 更新联系人结束
    public final static int MSG_CURRENT_DEVICE_ADDRESS = 3;
    public final static int MSG_DEVICE_CONNECT = 4;
    public final static int MSG_CONNECT_FAILE = 5;
    public static final int MSG_HFP_STATUS = 6;
    private boolean isDisconnect = false;
    private ArrayList<PhoneBook> contacts = new ArrayList<>();
    private XRecyclerView mRecyclerView;
    private TextView tv_contacts_count;
    private ImageView image_animation;
    private RelativeLayout rl_downloading;
    private TextView tv_device_disconnect;
    private TextView tv_download;
    private AlertDialog callPhoneDialog;
    private QMUIDialog qmuiMessageDialog;
    private QMUIDialog.MessageDialogBuilder builder;
    private ContactsRecyclerViewAdapter mRecyclerViewAdapter;
    private static Handler mHandler;
    private String phoneNumber = null;
    private ProgressDialog loadingDialog;
    private int itemCount = 20;
    private CompositeDisposable compositeDisposable;
    private AudioManager am;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mContext = this.getContext();
        am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mHandler = new MyHandler(this);
        compositeDisposable = new CompositeDisposable();
        View view = inflater.inflate(R.layout.fragment_phone, container, false);
        initView(view);
        Log.d(TAG, "onCreateView: " + GocsdkCallbackImp.hfpStatus);
        mHandler.sendEmptyMessageAtTime(102, 700);
        // Inflate the layout for this fragment
        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        reflashContactsData();
    }

    private void updateContacts(PhoneBook book) {
        contacts.add(book);
    }

    private void addAllPhoneNumberToDB() {
        compositeDisposable.add(Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                GocDatabase.getDefault().insetAll(contacts);
                emitter.onNext("ok");
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindUntilEvent(FragmentEvent.DESTROY))
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {

                    }
                }));
    }

    private void updatedContacts() {
        // 如果该数据库不为空，关闭该数据库，并赋值为null
//        Handler handlerFinish = TransActivity.getHandler();
//        if (handlerFinish != null) {
//            handlerFinish.sendEmptyMessage(TransActivity.MSG_FINISH);
//        }
        if (contacts.size() == 0) {
            dismissWaitingDialog();
            mHandler.sendEmptyMessage(MSG_DEVICE_CONNECT);
            ToastTool.showToast(R.string.agree_to_synchronize_contacts);
            return;
        }
        Log.d(TAG, "updatedContacts: isDisconnect " + isDisconnect);
        if (mRecyclerViewAdapter != null) {
            mRecyclerViewAdapter.updateData(contacts);
            mRecyclerViewAdapter.notifyDataSetChanged();
        } else {
            mRecyclerViewAdapter = new ContactsRecyclerViewAdapter(mContext, contacts);
            mRecyclerViewAdapter.setRecyclerViewOnItemListener(this);
            mRecyclerView.setAdapter(mRecyclerViewAdapter);
        }
        dismissWaitingDialog();
        addAllPhoneNumberToDB();
    }

    private void reflashContactsData() {
        try {
            Handler mainActivityHandler = BtHomeActivity.getHandler();
            if (mainActivityHandler == null) {
                return;
            }
            mainActivityHandler.sendEmptyMessage(BtHomeActivity.MSG_UPDATE_PHONEBOOK);
            // 判断联系人列表是否为空，不为空时清空它。
//            if (contacts.isEmpty() == false) {
            contacts.clear();
//            }
            GocDatabase.getDefault().clearPhonebook();
            // 联系人列表下载
            BtHomeActivity.getService().phoneBookStartUpdate();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void setLoadingListener() {
        mRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {

            }

            @Override
            public void onLoadMore() {
                itemCount += 20;
                mRecyclerViewAdapter.setLayoutItemCount(itemCount);
                mRecyclerView.loadMoreComplete();
            }
        });
    }

    private void saveDeviceAddress(String address) {
        SharePreferenceTool.getInstance().saveString("bt_address", address);
    }

    private String getDeviceAddress() {
        return SharePreferenceTool.getInstance().getString("bt_address");
    }


    private void updateDeviceAddress(String address) {
        String deviceAddress = getDeviceAddress();
        if (!deviceAddress.equals(address) && contacts.size() > 0
                && mRecyclerViewAdapter != null) {
            contacts.clear();
            mRecyclerViewAdapter.notifyDataSetChanged();
        }
        saveDeviceAddress(address);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.tv_download) {
            showWaitingDialog();
            reflashContactsData();
//            showDownLoading();
        }
    }

    @Override
    public void onItemClick(int position, PhoneBook book) {
        Log.d(TAG, "onItemClick:position " + position);
//        clickItemCallPhone(position);
        GocDatabase.getDefault().getName(book.getNumber());
        Log.d(TAG, "onItemClick: name " + GocDatabase.getDefault().getName(book.getNumber()));
        clickItemCallPhone(book);
    }

    @Override
    public void onItemLongClick(int position) {

    }

    @SuppressLint("HandlerLeak")
    public static class MyHandler extends Handler {
        private PhoneFragment fragment;

        public MyHandler(PhoneFragment firstItemFragment) {
            WeakReference<PhoneFragment> mWeakReference = new WeakReference<>(firstItemFragment);
            this.fragment = mWeakReference.get();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, "handleMessage: " + msg.what);
            switch (msg.what) {
                case MSG_PHONE_BOOK:
                    PhoneBook phonebook = (PhoneBook) msg.obj;
                    fragment.updateContacts(phonebook);
                    break;
                case MSG_PHONE_BOOK_DONE:
                    fragment.updatedContacts();
                    if (fragment.isDisconnect) {
                        fragment.showDisconnect();
                    } else {
                        fragment.showData();
                    }
                    break;
                case MSG_CURRENT_DEVICE_ADDRESS:
                    String address = (String) msg.obj;
                    fragment.updateDeviceAddress(address);
                    break;
                case MSG_DEVICE_CONNECT:
                    fragment.isDisconnect = false;
                    fragment.showConnect();
                    break;
                case MSG_CONNECT_FAILE:
                    fragment.isDisconnect = true;
                    fragment.showDisconnect();
                    break;
                case 102:
                    fragment.initState();
                    break;
                case MSG_HFP_STATUS:
                    int status = (Integer) msg.obj;
                    if (status >= 3) {
                        if (fragment.contacts.size() == 0) {
                            fragment.showConnect();
                        }
                    }
                    break;
            }
        }
    }

    private void showConnect() {
//        rl_downloading.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
        tv_device_disconnect.setVisibility(View.GONE);
        tv_download.setVisibility(View.VISIBLE);
    }

    private void showDownLoading() {
        TranslateAnimation animation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0.8f);
        animation.setDuration(1000);
        animation.setFillAfter(false);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.RESTART);
        image_animation.startAnimation(animation);
    }

    private void showData() {
//        rl_downloading.setVisibility(View.GONE);
        tv_download.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        tv_device_disconnect.setVisibility(View.GONE);
    }

    private void showDisconnect() {
        contacts.clear();
        if (mRecyclerViewAdapter != null) {
            mRecyclerViewAdapter.notifyDataSetChanged();
        }
//        tv_contacts_count.setText("");
//        rl_downloading.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
        tv_device_disconnect.setVisibility(View.VISIBLE);
        tv_download.setVisibility(View.GONE);
    }

    private void initRecyclerView() {
        LinearLayoutManager managerLocal = new LinearLayoutManager(mContext);
        managerLocal.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(managerLocal);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemViewCacheSize(20);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(mContext,
                RecyclerView.VERTICAL);
        ((DefaultItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations
                (false);
        itemDecoration.setDrawable((Objects.requireNonNull(ContextCompat.getDrawable(mContext,
                R.drawable.transparent_dividing_verticall_line))));
        mRecyclerView.addItemDecoration(itemDecoration);
        mRecyclerView.setPullRefreshEnabled(false);
//        mRecyclerViewAdapter = new ContactsRecyclerViewAdapter(mContext, contacts);
        setLoadingListener();
    }

    private void initView(View view) {
        mRecyclerView = view.findViewById(R.id.rcv_address_book);
        tv_device_disconnect = view.findViewById(R.id.tv_device_disconnect);
        tv_download = view.findViewById(R.id.tv_download);
        initRecyclerView();
        tv_download.setOnClickListener(this);
    }

    private void clickItemCallPhone(PhoneBook book) {
        // 获得点击联系人的信息
        String Name = book.getName();
        final String Num = book.getNumber();
        createCallOutDialog(Name, Num);
//        createCallOutQMUIDialog(Name, Num);
    }

    private void clickItemCallPhone(int position) {
        // 获得点击联系人的信息
        PhoneBook phoneBook = contacts.get(position);
        String Name = phoneBook.getName();
        final String Num = phoneBook.getNumber();
        createCallOutDialog(Name, Num);
    }

    private void createCallOutDialog(String Name, final String Num) {
        phoneNumber = Num;
        if (callPhoneDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(getString(R.string.want_to_call_this_number) + Name + ":" + Num);
            builder.setTitle(R.string.dialog_title_tips);
            builder.setPositiveButton(android.R.string.ok,
                    new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            arg0.dismiss();
                            placeCall();
                        }
                    });
            builder.setNegativeButton(android.R.string.cancel,
                    new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            callPhoneDialog = builder.create();
        }
        if (callPhoneDialog != null && !callPhoneDialog.isShowing()) {
            callPhoneDialog.setMessage(getString(R.string.phone_call_this_number) + Name + " : " + Num);
            callPhoneDialog.show();
        }
    }

    private void createCallOutQMUIDialog(String Name, final String Num) {
        phoneNumber = Num;
        if (qmuiMessageDialog == null) {
            builder = new QMUIDialog.MessageDialogBuilder(getContext());
            builder.setMessage("确定要拨打吗?" + Name + ":" + Num);
            builder.setTitle("提示");
            builder.addAction("取消", new QMUIDialogAction.ActionListener() {
                @Override
                public void onClick(QMUIDialog dialog, int index) {
                    dialog.dismiss();
                }
            });
            builder.addAction("确定", new QMUIDialogAction.ActionListener() {
                @Override
                public void onClick(QMUIDialog dialog, int index) {
                    dialog.dismiss();
                    placeCall();
                }
            });
            qmuiMessageDialog = builder.create();
        }
        if (qmuiMessageDialog != null && !qmuiMessageDialog.isShowing()) {
            builder.setMessage("确定要拨打该号码吗? " + Name + " : " + Num);
            qmuiMessageDialog.show();
        }
    }

    // 拨打正确的电话
    private void placeCall() {
        if (phoneNumber.length() == 0)
            return;
        if (PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)) {
            if (phoneNumber == null || !TextUtils.isGraphic(phoneNumber)) {
                return;
            }

            try {
                BtHomeActivity.getService().phoneDail(phoneNumber);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 显示正在等待的Dialog
     */
    public void showWaitingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new ProgressDialog(mContext, R.style.ProgressDialogStyle);
            loadingDialog.setMessage(mContext.getText(R.string.dialog_message));
            loadingDialog.setIndeterminate(true);
            loadingDialog.setCancelable(false);//点击返回或四周是否关闭dialog true关闭 false不可关闭
            loadingDialog.setOnKeyListener((dialog, keyCode, event) -> false);
//            WindowManager.LayoutParams params =
//                    loadingDialog.getWindow().getAttributes();
//            params.width = 450;
//            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
//            loadingDialog.getWindow().setAttributes(params);
        }
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    private void dismissWaitingDialog() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }

    public static Handler getHandler() {
        return mHandler;
    }

    private void initState() {
        if (GocsdkCallbackImp.hfpStatus >= 3) {
            showConnect();
        } else {
            showDisconnect();
        }
    }

    private void aa() {
        int result = am.requestAudioFocus(afChangeListener,
                // Use the music stream.
                AudioManager.STREAM_VOICE_CALL,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
        if (result == AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE) {

        }

        // 当播放完成时，建议调用abandonAudioFocus()方法来释放音频焦点，
        // 通知系统当前App不再需要音频焦点，解除OnAudioFocusChangeListener的注册。
        // Abandon audio focus when playback complete
        am.abandonAudioFocus(afChangeListener);
    }

    AudioManager.OnAudioFocusChangeListener afChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {


                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {

                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {

                    } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {

                    }
                }
            };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (loadingDialog != null) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }

        if (callPhoneDialog != null) {
            callPhoneDialog.dismiss();
            callPhoneDialog = null;
        }

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
            compositeDisposable.clear();
            compositeDisposable = null;
        }
    }

}