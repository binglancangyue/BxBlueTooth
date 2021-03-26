package com.bixin.bluetooth.view.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.RemoteException;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bixin.bluetooth.R;
import com.bixin.bluetooth.model.service.GocsdkCallbackImp;
import com.bixin.bluetooth.model.tools.ToastTool;
import com.bixin.bluetooth.view.activity.BtHomeActivity;


public class CallPhoneFragment extends Fragment implements OnClickListener {
    private BtHomeActivity activity;
    private StringBuilder stringBuilder;
    private TextView tv_phonenumber;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (BtHomeActivity) getActivity();
        stringBuilder = new StringBuilder();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = View.inflate(activity, R.layout.fragment_dial, null);
        ImageView iv_one = view.findViewById(R.id.iv_one);
        ImageView iv_two = view.findViewById(R.id.iv_two);
        ImageView iv_three = view.findViewById(R.id.iv_three);
        ImageView iv_four = view.findViewById(R.id.iv_four);
        ImageView iv_five = view.findViewById(R.id.iv_five);
        ImageView iv_six = view.findViewById(R.id.iv_six);
        ImageView iv_seven = view.findViewById(R.id.iv_seven);
        ImageView iv_eight = view.findViewById(R.id.iv_eight);
        ImageView iv_nine = view.findViewById(R.id.iv_nine);
        ImageView iv_xinghao = view.findViewById(R.id.iv_xinghao);
        ImageView iv_zero = view.findViewById(R.id.iv_zero);
        ImageView iv_jinghao = view.findViewById(R.id.iv_jinghao);
        ImageView iv_callout = view.findViewById(R.id.iv_callout);
        ImageView iv_delete = view.findViewById(R.id.iv_delete);
        tv_phonenumber = view.findViewById(R.id.tv_phonenumber);

        iv_one.setOnClickListener(this);
        iv_two.setOnClickListener(this);
        iv_three.setOnClickListener(this);
        iv_four.setOnClickListener(this);
        iv_five.setOnClickListener(this);
        iv_six.setOnClickListener(this);
        iv_seven.setOnClickListener(this);
        iv_eight.setOnClickListener(this);
        iv_nine.setOnClickListener(this);
        iv_xinghao.setOnClickListener(this);
        iv_zero.setOnClickListener(this);
        iv_jinghao.setOnClickListener(this);
        iv_callout.setOnClickListener(this);
        iv_delete.setOnClickListener(this);

        return view;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_one:
                stringBuilder.append("1");
                break;
            case R.id.iv_two:
                stringBuilder.append("2");
                break;
            case R.id.iv_three:
                stringBuilder.append("3");
                break;
            case R.id.iv_four:
                stringBuilder.append("4");
                break;
            case R.id.iv_five:
                stringBuilder.append("5");
                break;
            case R.id.iv_six:
                stringBuilder.append("6");
                break;
            case R.id.iv_seven:
                stringBuilder.append("7");
                break;
            case R.id.iv_eight:
                stringBuilder.append("8");
                break;
            case R.id.iv_nine:
                stringBuilder.append("9");
                break;
            case R.id.iv_xinghao:
                stringBuilder.append("*");
                break;
            case R.id.iv_zero:
                stringBuilder.append("0");
                break;
            case R.id.iv_jinghao:
                stringBuilder.append("#");
                break;
            case R.id.iv_callout:
                if (GocsdkCallbackImp.hfpStatus >= 3) {
                    String number = tv_phonenumber.getText().toString().trim();
                    if (TextUtils.isEmpty(number)) {
                        ToastTool.showToast(R.string.please_input_phone_number);
                    } else {
                        placeCall(number);
                    }
                } else {
                    ToastTool.showToast(R.string.please_connect_device);
                }
                break;
            case R.id.iv_delete:
                if (stringBuilder.length() > 0) {
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                }
                break;
        }
        tv_phonenumber.setText(stringBuilder.toString());
    }

    // 拨打正确的电话
    private void placeCall(String number) {
        if (number.length() == 0) return;
        if (BtHomeActivity.getService() == null) return;

        if (PhoneNumberUtils.isGlobalPhoneNumber(number)) {
            if (!TextUtils.isGraphic(number)) {
                return;
            }
            try {
                BtHomeActivity.getService().phoneDail(number);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
