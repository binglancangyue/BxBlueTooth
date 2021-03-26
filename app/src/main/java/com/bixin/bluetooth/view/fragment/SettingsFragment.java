package com.bixin.bluetooth.view.fragment;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bixin.bluetooth.R;
import com.bixin.bluetooth.model.bean.MyEditText;
import com.bixin.bluetooth.model.tools.ToastTool;
import com.bixin.bluetooth.view.activity.BtHomeActivity;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogView;

import java.lang.ref.WeakReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "SettingsFragment";
    private TextView tvDeviceName;
    private TextView tvPinCode;
    private Switch switchBt;
    private TextView tvState;
    private TextView tvConnectedDeviceName;
    public static final int MSG_DEVICE_NAME = 6;
    public static final int MSG_PIN_CODE = 7;
    public static final int MSG_AUTO_STATUS = 8;

    public static final int MSG_PAIRED_DEVICE = 0;
    public static final int MSG_CONNECT_ADDRESS = 1;
    public static final int MSG_CONNECT_SUCCESS = 2;
    public static final int MSG_CONNECT_FAILE = 3;
    public static final int MSG_CURRENT_STATUS = 4;
    public static final int MSG_HFP_STATUS = 5;
    private static MyHandler myHandler;
    private boolean isConnecting = false;
    private AlertDialog alertDialog;
    private QMUIDialog qmuidialog;
    private int dialogType = 0;
    private String pinCode;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
        initView(view);
        myHandler.sendEmptyMessageAtTime(102, 700);
    }

    private void init() {
        myHandler = new MyHandler(this);
        initState();
    }

    private void initView(View view) {
        tvConnectedDeviceName = view.findViewById(R.id.tv_connected_device_name);
        tvDeviceName = view.findViewById(R.id.tv_bt_name);
        tvPinCode = view.findViewById(R.id.tv_bt_code);
        switchBt = view.findViewById(R.id.switch_bt);
        tvState = view.findViewById(R.id.tv_bt_state);
        tvDeviceName.setOnClickListener(this);
        tvPinCode.setOnClickListener(this);
        String currentDeviceName = BtHomeActivity.currentDeviceName;
        if (currentDeviceName != null) {
            tvConnectedDeviceName.setText(currentDeviceName);
        }
        updateConnectState();
    }

    private void initState() {
        try {
            if (BtHomeActivity.getService() != null) {
                BtHomeActivity.getService().setAutoConnect();
                BtHomeActivity.getService().getPinCode();
                BtHomeActivity.getService().getCurrentDeviceName();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        int viewID = v.getId();
        if (viewID == R.id.tv_bt_code) {
            showCustomizeDialog(0);
//            showCustomizeDialogQ(0);
        }
        if (viewID == R.id.tv_bt_name) {
            showCustomizeDialog(1);
//            showCustomizeDialogQ(1);
        }
    }


    @SuppressLint("HandlerLeak")
    public static class MyHandler extends Handler {
        private SettingsFragment fragment;

        public MyHandler(SettingsFragment itemFragment) {
            WeakReference<SettingsFragment> mWeakReference = new WeakReference<>(itemFragment);
            this.fragment = mWeakReference.get();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, "handleMessage: msg " + msg.what);
            switch (msg.what) {
                case MSG_DEVICE_NAME:
                    String deviceName = (String) msg.obj;
                    try {
                        BtHomeActivity.getService().getCurrentDeviceName();
                        BtHomeActivity.getService().getPinCode();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    fragment.updateDeviceName(deviceName);
                    break;
                case MSG_PIN_CODE:
                    String pinCode = (String) msg.obj;
                    Log.d(TAG, "handleMessage:pinCode " + pinCode);
                    fragment.updateDeviceCode(pinCode);
                    break;

                case MSG_CONNECT_ADDRESS:
//                    address = (String) msg.obj;
                    break;
                case MSG_CONNECT_SUCCESS:
                    try {
                        BtHomeActivity.getService().getCurrentDeviceName();
                        BtHomeActivity.getService().getLocalName();
                        BtHomeActivity.getService().getPinCode();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    fragment.isConnecting = true;
                    fragment.updateConnectState();
                    Log.d("app", "connect success initData");

                    fragment.updateConnectedDeviceName((String) msg.obj);
                    break;
                case MSG_CONNECT_FAILE:
                    fragment.isConnecting = false;
                    fragment.updateConnectState();
                    Log.d("app", "connect failed initData");
                    break;

                case MSG_HFP_STATUS:
                    int status = (Integer) msg.obj;
                    if (status >= 3) {
                        try {
                            fragment.isConnecting = true;
                            BtHomeActivity.getService().getCurrentDeviceName();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        fragment.updateConnectState();
                    }
                    break;
                case 102:
                    fragment.updateConnectedDeviceName();
                    break;
            }
        }
    }

    private void updateConnectedDeviceName(String name) {
        tvConnectedDeviceName.setText(name);
        updateConnectState();
    }

    private void updateConnectedDeviceName() {
        if (BtHomeActivity.currentDeviceName != null) {
            tvConnectedDeviceName.setText(BtHomeActivity.currentDeviceName);
        }
        updateConnectState();
    }

    private void updateConnectState() {
        if (isConnecting) {
            tvState.setText(R.string.bt_connected);
        } else {
            tvState.setText(R.string.bt_no_connected);
        }
        String name = BtHomeActivity.currentDeviceName;
        String btName = BtHomeActivity.mLocalName;
        String btCode = BtHomeActivity.mPinCode;
        if (name != null) {
            tvConnectedDeviceName.setText(name);
        }
        if (btName != null) {
            tvDeviceName.setText(btName);
        }
        if (btCode != null) {
            tvPinCode.setText(btCode);
        }
    }

    private void updateDeviceName(String name) {
        tvDeviceName.setText(name);
        if (BtHomeActivity.currentDeviceName != null) {
            tvConnectedDeviceName.setText(BtHomeActivity.currentDeviceName);
        }
    }

    private void updateDeviceCode(String code) {
        tvPinCode.setText(code);
    }

    public static Handler getHandler() {
        return myHandler;
    }

    private void showCustomizeDialog(int type) {
        dialogType = type;
        if (alertDialog == null) {
            AlertDialog.Builder customizeDialog =
                    new AlertDialog.Builder(getActivity());
            final View dialogView = LayoutInflater.from(getActivity())
                    .inflate(R.layout.dialog_edit, null);
            customizeDialog.setTitle(R.string.edit_dialog_title_bt_name);
            customizeDialog.setView(dialogView);
            MyEditText edit_text =
                    dialogView.findViewById(R.id.edit_text);
            edit_text.setOnFinishComposingListener(new MyEditText.onFinishComposingListener() {

                @Override
                public void finishComposing() {
                    Log.d(TAG, "finishComposing: pinCode " + pinCode);
                    pinCode = edit_text.getText().toString();
                }
            });
            customizeDialog.setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String text = edit_text.getText().toString();
                            updateDialogCommit(text);
                            edit_text.setText("");
                            alertDialog.dismiss();
                        }
                    });
            customizeDialog.setNegativeButton(android.R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.dismiss();
                        }
                    });
            alertDialog = customizeDialog.create();
        }
        if (alertDialog != null && !alertDialog.isShowing()) {
            if (type == 0) {
                alertDialog.setTitle(R.string.edit_dialog_title_pin);
            } else {
                alertDialog.setTitle(R.string.edit_dialog_title_bt_name);
            }
            alertDialog.show();
        }

    }

    private void showCustomizeDialogQ(int type) {
        int mCurrentDialogStyle = com.qmuiteam.qmui.R.style.QMUI_Dialog;
        dialogType = type;
        if (qmuidialog == null) {
            QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(getActivity());
            builder.setTitle(R.string.edit_dialog_title_bt_name);
//            builder.setPlaceholder("在此输入昵称");
            builder.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.addAction("取消", new QMUIDialogAction.ActionListener() {
                @Override
                public void onClick(QMUIDialog dialog, int index) {
                    qmuidialog.dismiss();
                }
            });
            builder.addAction("确定", new QMUIDialogAction.ActionListener() {
                @Override
                public void onClick(QMUIDialog dialog, int index) {
                    String text = builder.getEditText().getText().toString();
                    updateDialogCommit(text);
                    qmuidialog.dismiss();
                }
            });
            qmuidialog = builder.create(mCurrentDialogStyle);
        }
        if (qmuidialog != null && !qmuidialog.isShowing()) {
            if (type == 0) {
                qmuidialog.setTitle(R.string.edit_dialog_title_pin);
            } else {
                qmuidialog.setTitle(R.string.edit_dialog_title_bt_name);
            }
            qmuidialog.show();
        }
    }

    private void updateDialogCommit(String text) {
        try {
            if (dialogType == 0) {
                if (!TextUtils.isEmpty(text)) {
                    if (checkText(text)) {
                        BtHomeActivity.getService().setPinCode(text);
                    } else {
                        ToastTool.showToast(R.string.please_input_number);
                    }
                    Log.d(TAG, "修改PIN" + text);
                }
            } else {
                if (!TextUtils.isEmpty(text)) {
                    BtHomeActivity.getService().setLocalName(text);
                    Log.d(TAG, "修改名字" + text);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private boolean checkText(String str) {
        String regEx = "[0-9]{4}";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isConnecting = false;
        if (myHandler != null) {
            myHandler.removeCallbacksAndMessages(null);
            myHandler = null;
        }
    }
}