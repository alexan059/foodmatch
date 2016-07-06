package com.fancyfood.foodmatch.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.fancyfood.foodmatch.helpers.HttpConnectionHelper;
import com.fancyfood.foodmatch.preferences.Constants;

import java.util.HashMap;

public class AuthenticationService extends IntentService {

    private static final String TAG = AuthenticationService.class.getSimpleName();

    /**
     * Creates an IntentService. Invoked by your subclass's constructor.
     */
    public AuthenticationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Intent service started.");

        String uri = intent.getDataString();

        HashMap<String, String> data = new HashMap<>();
        data.put("device_mac", uri);
        String token = HttpConnectionHelper.requestToken(data);

        // Has results
        if (token != null) {
            sendBroadcastDataStatus(Constants.TOKEN_RECEIVED, token);
        }
        else {
            Log.d(TAG, "Token not received");
        }
    }

    private void sendBroadcastDataStatus(String status, String token) {
        Intent localIntent = new Intent(Constants.BROADCAST_TOKEN)
                .putExtra(Constants.EXTENDED_DATA_STATUS, status)
                .putExtra(Constants.EXTENDED_DATA_TOKEN, token);

        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}
