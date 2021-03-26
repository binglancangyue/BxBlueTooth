package com.bixin.bluetooth.model.tools;

import android.content.Context;
import android.content.SharedPreferences;

import com.bixin.bluetooth.model.bean.BxBTApp;

/**
 * @author Altair
 * @date :2019.12.31 上午 10:13
 * @description:
 */
public class SharePreferenceTool {
    private static SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    private static class SingletonHolder {
        private static final SharePreferenceTool INSTANCE = new SharePreferenceTool();
    }

    public static SharePreferenceTool getInstance() {
        return SingletonHolder.INSTANCE;
    }


    public SharePreferenceTool() {
        mSharedPreferences = BxBTApp.getInstance().getSharedPreferences("bixin_config",
                Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
    }

    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    public void saveString(String key, String value) {
        mEditor.putString(key, value);
        mEditor.apply();
    }

    public String getString(String key) {
        return mSharedPreferences.getString(key, null);
    }

    public boolean getBoolean(String key, boolean value) {
        return mSharedPreferences.getBoolean(key, value);
    }

    public void setBoolean(String key, boolean value) {
        mEditor.putBoolean(key, value);
        mEditor.apply();
    }
}
