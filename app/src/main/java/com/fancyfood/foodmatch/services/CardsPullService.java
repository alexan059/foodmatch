package com.fancyfood.foodmatch.services;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.fancyfood.foodmatch.helpers.DataSourceHelper;
import com.fancyfood.foodmatch.helpers.HttpConnectionHelper;
import com.fancyfood.foodmatch.util.JSONCardsParser;
import com.fancyfood.foodmatch.preferences.Constants;
import com.fancyfood.foodmatch.models.Card;

import org.json.JSONArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
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

        URL location = getFullUrl(intent.getDataString());
        JSONArray results = HttpConnectionHelper.getJSONDataArray(location);

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

    private URL getFullUrl(String uri) {
        try {
            String url = Constants.API_ENTRY_POINT + uri + "?token=n5DUfSC72hPABeEhu89Ex63soJ2oJCQfTxlim8MC6oHVLlrutMa3xDjDursL";

            Log.d(TAG, url);

            return new URL(url);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
