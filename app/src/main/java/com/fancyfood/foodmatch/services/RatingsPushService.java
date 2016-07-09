package com.fancyfood.foodmatch.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.fancyfood.foodmatch.helpers.DataSourceHelper;
import com.fancyfood.foodmatch.helpers.HttpConnectionHelper;
import com.fancyfood.foodmatch.models.Rating;
import com.fancyfood.foodmatch.preferences.Preferences;
import com.fancyfood.foodmatch.util.JSONRatingsParser;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class RatingsPushService extends IntentService {

    private static final String TAG = RatingsPushService.class.getSimpleName();

    private DataSourceHelper database;

    /**
     * Creates an IntentService. Invoked by your subclass's constructor.
     */
    public RatingsPushService() {
        super(TAG);
        database = new DataSourceHelper(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Intent service started.");

//        Calendar calendar = Calendar.getInstance();
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY);
//        String newTimestamp = format.format(calendar.getTime());
//
//        String timestamp = Preferences.restoreSynctime(this);
//
//        Log.d(TAG, "Current Time: " + newTimestamp + " Old Time: " + timestamp);

        if (database.countNewRatings() > 0) {
            try {
                ArrayList<Rating> ratingsList = database.getNewRatings();
                JSONArray request = JSONRatingsParser.parse(ratingsList);

                Log.d(TAG, request.toString());

            if (HttpConnectionHelper.sendRatings(this, request)) {
                database.truncateRatings();
                //Preferences.storeSynctime(this, newTimestamp);
                //Log.d(TAG, "New sync time stored: " + newTimestamp + ". Replaced old: " + timestamp);
            } else {
                Log.d(TAG, "Sync unsuccessful.");
            }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "No data for sync available.");
        }


    }

}
