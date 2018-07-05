package com.co.dinofox.bubblebuilder.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.co.dinofox.bubblebuilder.R;

import hugo.weaving.DebugLog;

/**
 * Created by stefano on 04/05/16.
 */
public class MyIntentService extends IntentService {


    public MyIntentService(){
        super("MyIntentService");
    }




    public void showToast(String message) {
        final String msg = message;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    @DebugLog
    protected void onHandleIntent(Intent intent) {
        showToast(getString(R.string.option_disabled));
        SystemClock.sleep(300000);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        pref.edit().putBoolean(getString(R.string.sync_with_drive_programmed),false).apply();
        showToast(getString(R.string.try_log_again));

    }
}
