package com.fancyfood.foodmatch.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.fancyfood.foodmatch.authenticators.ApiAuthenticator;
import com.fancyfood.foodmatch.preferences.Constants;

public class TokenReceiver extends BroadcastReceiver {

    private static final String TAG = TokenReceiver.class.getSimpleName();

    public TokenReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        String status = intent.getStringExtra(Constants.EXTENDED_DATA_STATUS);
        String token = intent.getStringExtra(Constants.EXTENDED_DATA_TOKEN);

        switch (status) {
            case Constants.TOKEN_RECEIVED:
                ApiAuthenticator.setToken(context, token);
                break;
            //case Constants.DATA_NO_RESULTS:
            //    Toast.makeText(context, "Keine Ergebnisse", Toast.LENGTH_SHORT).show();
            //    break;
        }
    }
}
