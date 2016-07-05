package com.fancyfood.foodmatch.helpers;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.fancyfood.foodmatch.models.Card;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public final class HttpConnectionHelper {

    private static final String TAG = HttpConnectionHelper.class.getSimpleName();

    public static JSONArray getJSONDataArray(URL location) {
        try {
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

                    // Get result array
                    return parseJSONArray(stream);
//
                } else {
                    // Stream error
                    Log.e(TAG, connection.getErrorStream().toString());
                }
            } else {
                // Connection error
                String status = Integer.toString(connection.getResponseCode());
                Log.d(TAG, "Connection failed with response code: " + status);
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String downloadImage(Context context, URL location, String imageName) {
        // Get application context wrapper
        ContextWrapper contextWrapper = new ContextWrapper(context.getApplicationContext());
        // Path to app data
        File directory = contextWrapper.getDir("images", Context.MODE_PRIVATE);
        // Create image dir
        File path = new File(directory, imageName);

        FileOutputStream outputStream = null;

        try {
            // Download bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(location.openConnection().getInputStream());
            // Save bitmap
            outputStream = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, outputStream);
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        Log.d(TAG, directory.getAbsolutePath());

        // Return file directory
        return directory.getAbsolutePath();
    }

    static JSONArray parseJSONArray(InputStream stream) throws IOException, JSONException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        return new JSONArray(builder.toString());
    }

}
