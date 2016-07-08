package com.fancyfood.foodmatch.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.fancyfood.foodmatch.authenticators.ApiAuthenticator;
import com.fancyfood.foodmatch.helpers.DataSourceHelper;
import com.fancyfood.foodmatch.helpers.HttpConnectionHelper;
import com.fancyfood.foodmatch.models.Card;
import com.fancyfood.foodmatch.models.Rating;
import com.fancyfood.foodmatch.preferences.Constants;
import com.fancyfood.foodmatch.preferences.Preferences;
import com.fancyfood.foodmatch.util.JSONCardsParser;
import com.fancyfood.foodmatch.util.JSONRatingsParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY);
        String newTimestamp = format.format(calendar.getTime());

        String timestamp = Preferences.restoreSynctime(this);

        if (database.countNewRatings(timestamp) > 0) {
            try {
                ArrayList<Rating> ratingsList = database.getNewRatings(timestamp);
                JSONArray ratings = JSONRatingsParser.createJSONArray(ratingsList);

                JSONObject request = new JSONObject();

                request.put("device_id", ApiAuthenticator.getAndroidId(this));
                request.put("ratings", ratings);

                Log.d(TAG, request.toString());

            if (HttpConnectionHelper.sendRatings(this, request)) {
                //sendBroadcastDataStatus(Constants.DATA_RECEIVED);
                Preferences.storeSynctime(this, newTimestamp);
                Log.d(TAG, "New sync time stored: " + newTimestamp + ". Replaced old: " + timestamp);
            } else {
                //sendBroadcastDataStatus(Constants.DATA_PARSE_ERROR);
                Log.d(TAG, "Sync unsuccessful.");
            }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "No data for sync available.");
        }


    }

    /*private void sendBroadcastDataStatus(String status) {
        Intent localIntent = new Intent(Constants.BROADCAST_ACTION)
                .putExtra(Constants.EXTENDED_DATA_STATUS, status);

        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }*/
}
