package com.fancyfood.foodmatch.services;

import android.app.IntentService;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.fancyfood.foodmatch.data.DishesDataSource;
import com.fancyfood.foodmatch.helpers.HttpConnectionHelper;
import com.fancyfood.foodmatch.preferences.Constants;
import com.fancyfood.foodmatch.models.Card;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class CardsPullService extends IntentService {

    private static final String TAG = CardsPullService.class.getSimpleName();

    private DishesDataSource dataSource;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public CardsPullService() {
        super(TAG);
        dataSource = new DishesDataSource(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Intent service started.");
        URL location = getFullUrl(intent.getDataString());
        JSONArray results = HttpConnectionHelper.getJSONDataArray(location);

        // Has results
        if (results != null && results.length() > 0) {

            JSONObject restaurant = null;
            try {
                restaurant = results.getJSONObject(0);
                Log.d(TAG, restaurant.toString());

                Card card = JSONtoCard(restaurant);

                dataSource.insertDish(card);
                dataSource.getAllDishes();

                sendBroadcastDataStatus(Constants.DATA_RECEIVED);
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }


        }
        else {
            sendBroadcastDataStatus(Constants.DATA_NO_RESULTS);
        }

        //JSONObject object = array.getJSONObject(0);
        //String name = object.getString("name");
        //Log.d(TAG, name);
    }

    private void sendBroadcastDataStatus(String status) {
        Intent localIntent = new Intent(Constants.BROADCAST_ACTION)
                .putExtra(Constants.EXTENDED_DATA_STATUS, status);

        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private Drawable loadImage(String directory, String imageName) {
        Drawable image = null;

        try {
            File imageFile = new File(directory, imageName);
            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(imageFile));

            image = new BitmapDrawable(getResources(), bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return image;
    }

    private Card JSONtoCard(JSONObject dish) throws JSONException, IOException {
        JSONObject restaurant = dish.getJSONObject("restaurant");
        String _id = dish.getString("_id");
        String name = dish.getString("name");
        String locationName = restaurant.getString("name");

        String locationReference = restaurant.getString("_id");

        JSONObject geo = restaurant.getJSONObject("geo");
        JSONArray coordinates = geo.getJSONArray("coordinates");
        Location location = new Location("");
        location.setLatitude(coordinates.getDouble(0));
        location.setLongitude(coordinates.getDouble(1));

        JSONObject media = dish.getJSONObject("media");
        String fileName = media.getString("file");
        URL imageUrl = getFullUrl("restaurants/media/" + fileName);

        HttpConnectionHelper.downloadImage(this, imageUrl, fileName);

        //Drawable image = loadImage(directory, fileName);

        Card card = new Card(_id, location, null, name, locationName, 0, 0);
        card.setLocationReference(locationReference);
        card.setImageName(fileName);

        return card;
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
