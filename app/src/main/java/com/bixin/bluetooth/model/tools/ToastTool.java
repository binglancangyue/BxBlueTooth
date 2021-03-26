package com.bixin.bluetooth.model.tools;

import android.widget.Toast;

import com.bixin.bluetooth.model.bean.BxBTApp;


public class ToastTool {
    private static Toast toast = null;

    public static ToastTool getInstance() {
        return SingletonHolder.sInstance;
    }

    private static class SingletonHolder {
        private static final ToastTool sInstance = new ToastTool();
    }

    public static void showToast(int text) {
        if (toast == null) {
            toast = Toast.makeText(BxBTApp.getInstance(), text, Toast.LENGTH_SHORT);
        } else {
            toast.setText(text);
            toast.setDuration(Toast.LENGTH_SHORT);
        }
        toast.show();
    }
}
