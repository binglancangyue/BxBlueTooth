package com.bixin.bluetooth.model.bean;

import android.app.Application;

public class BxBTApp extends Application {
    private static BxBTApp application;

    @Override
    public void onCreate() {
        super.onCreate();
        this.application = this;
    }

    public static BxBTApp getInstance() {
        return application;
    }
}
