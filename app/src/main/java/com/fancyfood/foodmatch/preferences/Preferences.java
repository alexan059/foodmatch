package com.fancyfood.foodmatch.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public final class Preferences {

    public static final String PREFERENCES = "com.fancyfood.foodmatch.preferences";

    // Defaults
    public static final int DEFAULT_RADIUS = 5;

    public static int getRadius(Context context) {// Restore preferences
        SharedPreferences settings = context.getSharedPreferences(PREFERENCES, 0);
        return settings.getInt("radius", DEFAULT_RADIUS);
    }

    public static void storeRadius(Context context, int radius) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCES, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("radius", radius);
        editor.apply();
    }

}
