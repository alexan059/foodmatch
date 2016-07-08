package com.fancyfood.foodmatch.helpers;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.fancyfood.foodmatch.authenticators.ApiAuthenticator;
import com.fancyfood.foodmatch.models.Card;
import com.fancyfood.foodmatch.models.Rating;
import com.fancyfood.foodmatch.preferences.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class HttpConnectionHelper {

    private static final String TAG = HttpConnectionHelper.class.getSimpleName();

    public static String requestToken(String deviceId, String timestamp, String hash) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        String token = null;

        try {
            URL url = new URL(Constants.API_ENTRY_POINT + "users");

            connection = (HttpURLConnection) url.openConnection();

            connection.setDoOutput(true);
            connection.setReadTimeout(10000);
            connection.setChunkedStreamingMode(0); // optimizing performance
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/text; charset=utf-8");

            connection.setRequestProperty("FM-API-Device-ID", deviceId);
            connection.setRequestProperty("FM-API-Timestamp", timestamp);
            connection.setRequestProperty("FM-API-Hash", hash);

            Log.d(TAG, "Response code on request token: " + String.valueOf(connection.getResponseCode()));

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                inputStream = connection.getInputStream();
                String result = readStream(inputStream);
                JSONObject response = new JSONObject(result);

                if (response.getString("status").equals("success")) {
                    token = response.getString("data");
                }

            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return token;
    }

    public static JSONArray requestDishes(Context context, String uri) {
        URL location = getFullURL(context, uri);

        if (location == null) return null;

        HttpURLConnection connection = null;
        InputStream stream = null;
        JSONArray response = null;

        try  {

            // Open connection
            connection = (HttpURLConnection) location.openConnection();
            // Connect
            connection.connect();

            // Check if connection is ok. HTTP Status 200
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                // Get input stream
                stream = connection.getInputStream();

                // If stream has content
                if (stream != null) {

                    // Get result array
                    String result = readStream(stream);
                    response = new JSONArray(result);

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
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return response;
    }

    public static boolean sendRatings(Context context, JSONObject ratings) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        boolean success = false;

        URL location = getFullURL(context, "statistics");

        if (location == null) return false;

        try {
            connection = (HttpURLConnection) location.openConnection();

            connection.setDoOutput(true);
            connection.setReadTimeout(10000);
            connection.setChunkedStreamingMode(0); // optimizing performance
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            outputStream = new BufferedOutputStream(connection.getOutputStream());
            writeStream(outputStream, ratings);

            Log.d(TAG, "Response Code: " + connection.getResponseCode());

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                inputStream = connection.getInputStream();
                String result = readStream(inputStream);
                JSONObject response = new JSONObject(result);

                if (response.getString("status").equals("success")) {
                    String message = response.getString("data");

                    Log.d(TAG, message);

                    success = true;
                }
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return success;
    }

    public static void downloadImages(Context context, ArrayList<Card> cardsList) {
        for (Card card : cardsList) {
            String fileName = card.getImageName();
            String uri = "restaurants/media/";
            URL url = getFullURL(context, uri + fileName);
            downloadImage(context, url, fileName);
        }

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

    public static URL getFullURL(Context context, String uri) {
        String token = ApiAuthenticator.getToken(context);

        try {
            String url = Constants.API_ENTRY_POINT + uri + "?token=" + token;

            Log.d(TAG, url);

            return new URL(url);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String readStream(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        return builder.toString();
    }

    private static void writeStream(OutputStream stream, JSONObject object) throws IOException {
        stream.write(object.toString().getBytes());
        stream.flush();
    }

}
