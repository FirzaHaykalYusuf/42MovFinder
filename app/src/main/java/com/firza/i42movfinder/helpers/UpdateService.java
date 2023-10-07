package com.firza.i42movfinder.helpers;

import android.app.IntentService;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class UpdateService extends IntentService {

    public static final String ACTION_UPDATE_COMPLETED = "com.firza.i42movfinder.UPDATE_COMPLETED";
    DataHelper dataHelper;

    public UpdateService() {
        super("UpdateService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dataHelper = new DataHelper(getBaseContext());
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        dataHelper.clearData();
        // Lakukan pembaruan Anda di sini, misalnya menghapus data lama dan menambahkan data baru

        // Kirim broadcast ketika selesai
        Intent localIntent = new Intent(ACTION_UPDATE_COMPLETED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}

