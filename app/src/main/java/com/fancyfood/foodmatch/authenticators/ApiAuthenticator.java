package com.fancyfood.foodmatch.authenticators;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import com.fancyfood.foodmatch.preferences.Preferences;
import com.fancyfood.foodmatch.services.AuthenticationService;

public final class ApiAuthenticator {

    private static final String TAG = ApiAuthenticator.class.getSimpleName();

    public static String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getToken(Context context) {
        //return "n5DUfSC72hPABeEhu89Ex63soJ2oJCQfTxlim8MC6oHVLlrutMa3xDjDursL";

        String token = Preferences.restoreToken(context);

        if (token == null) {
            Intent intent = new Intent(context, AuthenticationService.class);
            intent.setData(Uri.parse(getAndroidId(context)));
            context.startService(intent);

            while (token == null) {

                try {
                    Log.i(TAG, "Timeout 5 seconds.");
                    Thread.sleep(5000);
                    token = Preferences.restoreToken(context);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }

            }
        }

        Log.d(TAG, "Token: " + token);

        return token;
    }

    public static void setToken(Context context, String token) {
        Preferences.storeToken(context, token);
    }
}
