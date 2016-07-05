package com.fancyfood.foodmatch.activities;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.fancyfood.foodmatch.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.prefs.PreferencesFactory;

public class JSONActivity extends AppCompatActivity {

    public TextView textView;
    public ImageView imageView;

    public String token = "OkgMGQiOz1gUiiaXt899fTePbMYiCTkjiV5P3hp8wBV2B5dPh0gsv6WOi6UC"; // empty, if first call
    public static final String URL = "http://api.collective-art.de/";

    private static final String LOG_TAG = JSONActivity.class.getSimpleName();

    private static final int HTTP_STORE = 0;
    private static final int HTTP_FETCH = 1;
    private static final int HTTP_STORE_FETCH = 2;

    private static final String JSON_SUCCESS = "success";
    private static final String JSON_ERROR = "error";

    private String androidID;

    private Map<String, String> data = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json);

        androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
        }

        Button button = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.textView);
        imageView = (ImageView) findViewById(R.id.imageView);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*

                ###FÜR DICH ALEX###

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                boolean firstAppCall = preferences.getBoolean(PREFERENCE_FIRST_APP_CALL, true);
                preferences.edit().putBoolean(PREFERENCE_FIRST_APP_CALL, false).commit();

                */

                /* FIRST APP CALL */

                data = new HashMap<String, String>();
                data.put("device_mac", androidID);

                new JSONTask(URL + "users", "", data, HTTP_STORE_FETCH).execute();

                /* END: FIRST APP CAL*/

                /* FETCHING ALL RESTAURANTS */
                /*
                data = new HashMap<String, String>();
                new JSONTask(URL + "restaurants", "token=" + token, data, HTTP_FETCH).execute();
                */
                /* END FETCHING ALL RESTAURANTS */

                /* FETCHING RESTAURANTS WITH GEO & RADIUS */
                /*
                data = new HashMap<String, String>();
                new JSONTask(URL + "restaurants/13.5264438/52.4569312/2000", "token=" + token, data, HTTP_FETCH).execute();
                */
                /* END: FETCHING RESTAURANTS WITH GEO & RADIUS */

                /* STORE/ UPDATE WITH "NO RELEVANT RESPONSE" */
                /*
                data = new HashMap<String, String>();
                data.put("dish_id", "!!!DISH_ID!!!");
                data.put("like", "0");

                new JSONTask(URL + "statistics", "token=" + token, data, HTTP_STORE).execute();
                /*
                /* END: STORE/ UPDATE WITH "NO RELEVANT RESPONSE" */
            }
        });
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
    private void writeStream(OutputStream out, String serializedData) throws IOException {

        out.write(serializedData.getBytes());
        out.flush();
    }
    private String readStream(InputStream inputStream) throws IOException {

        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream), 1000);
        for (String line = reader.readLine(); line != null; line =reader.readLine()) {

            stringBuilder.append(line);
        }
        inputStream.close();

        return stringBuilder.toString();
    }
    public class JSONTask extends AsyncTask<String, String, String> {

        public Bitmap bitmap = null;

        private String finalDomain;
        private String finalQueryString;
        private Map finalData;
        private Integer finalMethod; // store or fetch data or both?
        private String finalFullUrl;

        private String bufferedInput = null;

        JSONTask(String finalDomain, String finalQueryString, Map finalData, Integer finalMethod) {
            this.finalDomain = finalDomain;
            this.finalQueryString = finalQueryString;
            this.finalData = finalData;
            this.finalMethod = finalMethod;
        }

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;
            OutputStream outputStream = null;
            InputStream inputStream = null;

            JSONObject parentObject = null;
            JSONArray parentArray = null;
            JSONObject finalObject = null;
            JSONArray finalArray = null;

            try {

                if(finalQueryString.isEmpty()) {
                    finalFullUrl = finalDomain;
                } else {
                    finalFullUrl = finalDomain + "?" + finalQueryString;
                }

                Log.d(LOG_TAG, "Trying to request URL: " + finalFullUrl);

                URL url = new URL(finalFullUrl);

                connection = (HttpURLConnection) url.openConnection();

                switch(finalMethod) {

                    case HTTP_STORE_FETCH:

                        Log.d(LOG_TAG, "No valid token found in preferences!");

                        connection.setDoOutput(true);
                        connection.setReadTimeout(10000);
                        connection.setChunkedStreamingMode(0); // optimizing performance
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Content-Type", "application/text; charset=utf-8");

                        outputStream = new BufferedOutputStream(connection.getOutputStream());
                        writeStream(outputStream, mapToString(finalData));

                        Log.d(LOG_TAG, "Response Code: " + connection.getResponseCode());

                        if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                            inputStream = new BufferedInputStream(connection.getInputStream());
                            bufferedInput = readStream(inputStream);

                            parentObject = new JSONObject(bufferedInput.toString());
                            if(parentObject.getString("status").equals(JSON_SUCCESS)) {

                                token = parentObject.getString("data");

                                Log.d(LOG_TAG, "Generated token: " + token);
                            } // else custom error, but no exception
                        } // else error from lumen, exception

                        break;
                    case HTTP_FETCH:

                        connection.setDoInput(true);
                        connection.setReadTimeout(10000);
                        connection.setChunkedStreamingMode(0); // optimizing performance
                        connection.setRequestMethod("GET");

                        Log.d(LOG_TAG, "Response Code: " + connection.getResponseCode());

                        if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                            inputStream = new BufferedInputStream(connection.getInputStream());
                            bufferedInput = readStream(inputStream);

                            parentArray = new JSONArray(bufferedInput.toString());

                            for (int i = 0; i < parentArray.length(); i++) {

                                finalObject = parentArray.getJSONObject(i).getJSONObject("media");
                                String fileName = finalObject.getString("file");

                                URL imageUrl = new URL(URL + "restaurants/media/" + fileName + "?token=" + token);
                                bitmap = BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream());

                                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 60, bytes);

                                Log.d(LOG_TAG, "Storing image: " + fileName);
                                File file = new File(Environment.getExternalStorageDirectory() + File.separator + fileName);
                                file.createNewFile();

                                FileOutputStream fileOutput = new FileOutputStream(file);

                                fileOutput.write(bytes.toByteArray());
                                fileOutput.close();
                            } // else custom error, but no exception
                        } // else error from lumen, exception

                        break;

                    case HTTP_STORE:

                        connection.setDoOutput(true);
                        connection.setReadTimeout(10000);
                        connection.setChunkedStreamingMode(0); // optimizing performance
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Content-Type", "application/text; charset=utf-8");

                        outputStream = new BufferedOutputStream(connection.getOutputStream());
                        writeStream(outputStream, mapToString(finalData));

                        Log.d(LOG_TAG, "Response Code: " + connection.getResponseCode());

                        if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                            inputStream = new BufferedInputStream(connection.getInputStream());
                            bufferedInput = readStream(inputStream);

                            parentObject = new JSONObject(bufferedInput.toString());
                            if(parentObject.getString("status").equals(JSON_SUCCESS)) {

                                Log.d(LOG_TAG, "Statistics updated: " + token);
                            } // else custom error, but no exception
                        } // else error from lumen, exception

                        break;
                }

                return "done fetching this shit...";

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            textView.setText(result);
            imageView.setImageBitmap(bitmap);
        }
    }
}
