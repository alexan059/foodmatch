package com.fancyfood.foodmatch.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.fancyfood.foodmatch.helpers.DataSourceHelper;
import com.fancyfood.foodmatch.helpers.HttpConnectionHelper;
import com.fancyfood.foodmatch.util.JSONCardsParser;
import com.fancyfood.foodmatch.preferences.Constants;
import com.fancyfood.foodmatch.models.Card;

import org.json.JSONArray;

import java.util.ArrayList;

public class CardsPullService extends IntentService {

    private static final String TAG = CardsPullService.class.getSimpleName();

    private DataSourceHelper database;

    /**
     * Creates an IntentService. Invoked by your subclass's constructor.
     */
    public CardsPullService() {
        super(TAG);
        database = new DataSourceHelper(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Intent service started.");

        String uri = intent.getDataString();
        JSONArray results = HttpConnectionHelper.requestDishes(this, uri);

        // Has results
        if (results != null && results.length() > 0) {
            ArrayList<Card> cardsList = JSONCardsParser.parse(results);

            HttpConnectionHelper.downloadImages(this, cardsList);

            Log.d(TAG, Integer.toString(results.length()) + " results.");

            // Parse success
            if (cardsList != null) {
                // Add cards to database
                database.addCardsBatch(cardsList);
                // Notify activity for received data
                sendBroadcastDataStatus(Constants.DATA_RECEIVED);
            } else {
                sendBroadcastDataStatus(Constants.DATA_PARSE_ERROR);
            }

        }
        else {
            sendBroadcastDataStatus(Constants.DATA_NO_RESULTS);
        }
    }

    private void sendBroadcastDataStatus(String status) {
        Intent localIntent = new Intent(Constants.BROADCAST_ACTION)
                .putExtra(Constants.EXTENDED_DATA_STATUS, status);

        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}
