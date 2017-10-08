package com.fancyfood.foodmatch.authenticators;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import com.fancyfood.foodmatch.preferences.Constants;
import com.fancyfood.foodmatch.preferences.Preferences;
import com.fancyfood.foodmatch.services.AuthenticationService;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public final class ApiAuthenticator {

    private static final String TAG = ApiAuthenticator.class.getSimpleName();

    private static final String SEPARATOR = "|";

    public static String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getToken(Context context) {
        String token = Preferences.restoreToken(context);

        if (token == null) {

            String deviceId = getAndroidId(context);

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY);
            String timestamp = format.format(calendar.getTime());

            Log.d(TAG, "Datetime: " + timestamp);

            String hash = Constants.API_SECRET + SEPARATOR + deviceId + SEPARATOR + timestamp;

            //Log.d(TAG, hash); // Never show secret only for debug

            String hashed = null;
            try {
                hashed = SHA1(hash);
            } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                e.printStackTrace();
                Log.d(TAG, "String can't be hashed on this device.");
            }

            Log.d(TAG, hashed);

            Intent intent = new Intent(context, AuthenticationService.class);
            intent.putExtra("device-id", deviceId);
            intent.putExtra("timestamp", timestamp);
            intent.putExtra("hash", hashed);
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

    private static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9)) {
                    buf.append((char) ('0' + halfbyte));
                }
                else {
                    buf.append((char) ('a' + (halfbyte - 10)));
                }
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        }
        return buf.toString();
    }


    public static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] sha1hash = new byte[40];
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        sha1hash = md.digest();
        return convertToHex(sha1hash);
    }
}
