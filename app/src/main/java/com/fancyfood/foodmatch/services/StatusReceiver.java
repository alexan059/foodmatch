package com.fancyfood.foodmatch.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.fancyfood.foodmatch.authenticators.ApiAuthenticator;
import com.fancyfood.foodmatch.core.CoreApplication;
import com.fancyfood.foodmatch.preferences.Constants;

public class StatusReceiver extends BroadcastReceiver {

    private static final String TAG = StatusReceiver.class.getSimpleName();

    public StatusReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        String status = intent.getStringExtra(Constants.EXTENDED_DATA_STATUS);
        Log.d(TAG, status);


        switch (status) {
            case Constants.GPS_ENABLED:
                CoreApplication.getInstance().getGoogleApiHelperInstance().connect();
                break;
            //case Constants.DATA_NO_RESULTS:
            //    Toast.makeText(context, "Keine Ergebnisse", Toast.LENGTH_SHORT).show();
            //    break;
        }
    }
}
