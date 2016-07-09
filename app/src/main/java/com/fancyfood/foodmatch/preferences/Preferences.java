package com.fancyfood.foodmatch.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public final class Preferences {

    public static final String PREFERENCES = "com.fancyfood.foodmatch.preferences";

    // Defaults
    public static final int DEFAULT_RADIUS = 5;
    public static final String DEFAULT_TOKEN = null;
    //public static final String DEFAULT_SYNCTIME = "1970-01-01 00:00:00";

    public static int restoreRadius(Context context) {// Restore preferences
        SharedPreferences settings = context.getSharedPreferences(PREFERENCES, 0);
        return settings.getInt("radius", DEFAULT_RADIUS);
    }

    public static void storeRadius(Context context, int radius) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCES, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("radius", radius);
        editor.apply();
    }

    public static String restoreToken(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCES, 0);
        return settings.getString("token", DEFAULT_TOKEN);
    }

    public static void storeToken(Context context, String token) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCES, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("token", token);
        editor.apply();
    }

//    public static String restoreSynctime(Context context) {
//        SharedPreferences settings = context.getSharedPreferences(PREFERENCES, 0);
//        return settings.getString("data_sync", DEFAULT_SYNCTIME);
//    }
//
//    public static void storeSynctime(Context context, String timestamp) {
//        SharedPreferences settings = context.getSharedPreferences(PREFERENCES, 0);
//        SharedPreferences.Editor editor = settings.edit();
//        editor.putString("data_sync", timestamp);
//        editor.apply();
//    }
}
