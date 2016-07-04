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
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.fancyfood.foodmatch.Constants;
import com.fancyfood.foodmatch.models.Card;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public CardsPullService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Intent service started.");

        try {
            // Create URL
            URL location = getFullUrl(intent.getDataString());
            // Parse to connection
            HttpURLConnection connection = (HttpURLConnection) location.openConnection();
            // Connect
            connection.connect();

            // Check if connection is ok. HTTP Status 200
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                // Get input stream
                InputStream stream = connection.getInputStream();

                // If stream has content
                if (stream != null) {
                    JSONArray results = parseJSONArray(stream);
                    // Has results
                    if (results.length() > 0) {

                        JSONObject restaurant = results.getJSONObject(0);
                        Card card = JSONtoCard(restaurant);

                        handleResponse(card);

                        Log.d(TAG, card.toString());

                    }
                    else {
                        Log.d(TAG, "Objects: " + Integer.toString(results.length())); // TODO handle empty array
                    }

                    //JSONObject object = array.getJSONObject(0);
                    //String name = object.getString("name");
                    //Log.d(TAG, name);
                }
                else {
                    // Stream error
                    Log.e(TAG, connection.getErrorStream().toString());
                }
            } else {
                // Connection error
                String status = Integer.toString(connection.getResponseCode());
                Log.d(TAG, "Connection failed with response code: " + status);
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleResponse(Card card) {
        Intent localIntent = new Intent(Constants.BROADCAST_ACTION)
                .putExtra(Constants.EXTENDED_DATA_STATUS, card.getDish());
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private JSONArray parseJSONArray(InputStream stream) throws IOException, JSONException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        return new JSONArray(builder.toString());
    }

    private String downloadImage(String imageName) throws IOException {
        URL url = getFullUrl("restaurants/media/" + imageName);

        // Get application context wrapper
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        // Path to app data
        File directory = contextWrapper.getDir("images", Context.MODE_PRIVATE);
        // Create image dir
        File path = new File(directory, imageName);

        FileOutputStream outputStream = null;

        try {
            // Download bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            // Save bitmap
            outputStream = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, outputStream);
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null)
                outputStream.close();
        }

        // Return file directory
        return directory.getAbsolutePath();
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

    private Card JSONtoCard(JSONObject restaurant) throws JSONException, IOException {
        JSONArray dishes = restaurant.getJSONArray("dishes");
        JSONObject dish = dishes.getJSONObject(0);
        String _id = dish.getString("_id");
        String name = dish.getString("name");
        String locationName = restaurant.getString("name");

        JSONObject geo = restaurant.getJSONObject("geo");
        JSONArray coordinates = geo.getJSONArray("coordinates");
        Location location = new Location("");
        location.setLatitude(coordinates.getDouble(0));
        location.setLongitude(coordinates.getDouble(1));

        JSONObject media = dish.getJSONObject("media");
        String fileName = media.getString("file");
        String directory = downloadImage(fileName);
        Drawable image = loadImage(directory, fileName);

        return new Card(_id, location, image, name, locationName, 0, 0);
    }

    private URL getFullUrl(String uri) throws MalformedURLException {
        String url = Constants.API + uri + "?token=" + Constants.API_TOKEN;
        return new URL(url);
    }
}
