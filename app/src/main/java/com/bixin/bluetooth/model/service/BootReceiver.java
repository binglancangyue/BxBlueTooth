package com.bixin.bluetooth.model.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("liwinner","action : "+action);
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(new Intent(context,GocsdkService.class));
            } else {
                context.startService(new Intent(context,GocsdkService.class));
            }
        }
    }
}
