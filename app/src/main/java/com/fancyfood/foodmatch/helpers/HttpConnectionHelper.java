package com.fancyfood.foodmatch.helpers;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.fancyfood.foodmatch.authenticators.ApiAuthenticator;
import com.fancyfood.foodmatch.models.Card;
import com.fancyfood.foodmatch.preferences.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class HttpConnectionHelper {

    private static final String TAG = HttpConnectionHelper.class.getSimpleName();

    public static String requestToken(Map<String, String> data) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        String token = null;

        try {
            URL url = new URL(Constants.API_ENTRY_POINT + "users");

            connection = (HttpURLConnection) url.openConnection();

            connection.setDoOutput(true);
            connection.setReadTimeout(10000);
            connection.setChunkedStreamingMode(0); // optimizing performance
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/text; charset=utf-8");

            outputStream = new BufferedOutputStream(connection.getOutputStream());

            writeStream(outputStream, mapToString(data));

            Log.d(TAG, "Response code on request token: " + String.valueOf(connection.getResponseCode()));

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                inputStream = new BufferedInputStream(connection.getInputStream());
                String bufferedInput = readStream(inputStream);

                JSONObject parentObject = new JSONObject(bufferedInput);

                if(parentObject.getString("status").equals("success")) {
                    token = parentObject.getString("data");
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
                if (outputStream != null) {
                    outputStream.close();
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
        JSONArray resultArray = null;

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
                    resultArray = parseJSONArray(stream);

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

        return resultArray;
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

    public static JSONArray parseJSONArray(InputStream stream) throws IOException, JSONException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        return new JSONArray(builder.toString());
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

    public static String mapToString(Map<String, String> map) {

        StringBuilder stringBuilder = new StringBuilder();

        for (String key : map.keySet()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append("&");
            }
            String value = map.get(key);
            try {
                stringBuilder.append((key != null ? URLEncoder.encode(key, "UTF-8") : ""));
                stringBuilder.append("=");
                stringBuilder.append(value != null ? URLEncoder.encode(value, "UTF-8") : "");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("This method requires UTF-8 encoding support", e);
            }
        }
        return stringBuilder.toString();
    }

    private static void writeStream(OutputStream out, String serializedData) throws IOException {
        out.write(serializedData.getBytes());
        out.flush();
    }

    private static String readStream(InputStream inputStream) throws IOException {

        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream), 1000);
        for (String line = reader.readLine(); line != null; line =reader.readLine()) {

            stringBuilder.append(line);
        }
        inputStream.close();

        return stringBuilder.toString();
    }
}
