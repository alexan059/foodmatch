package com.fancyfood.foodmatch.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.fancyfood.foodmatch.core.CoreApplication;
import com.fancyfood.foodmatch.helpers.GoogleApiLocationHelper;
import com.fancyfood.foodmatch.helpers.HttpConnectionHelper;
import com.fancyfood.foodmatch.preferences.Constants;
import com.fancyfood.foodmatch.preferences.Preferences;

import java.util.HashMap;

public class StatusService extends IntentService {

    private static final String TAG = StatusService.class.getSimpleName();

    /**
     * Creates an IntentService. Invoked by your subclass's constructor.
     */
    public StatusService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String uri = intent.getDataString();

       switch (uri) {
           case Constants.GPS_DISBALED:
               CoreApplication.getInstance().getGoogleApiHelperInstance().disconnect();
               waitForGps();
               break;
       }
    }

    private void sendBroadcastDataStatus(String status) {
        Intent localIntent = new Intent(Constants.BROADCAST_STATUS)
                .putExtra(Constants.EXTENDED_DATA_STATUS, status);

        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private void waitForGps() {
        while (!GoogleApiLocationHelper.isGpsEnabled(this, false, false)) {

            try {
                Log.i(TAG, "Timeout 5 seconds.");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }

        }

        sendBroadcastDataStatus(Constants.GPS_ENABLED);
    }
}
