package com.fancyfood.foodmatch.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fancyfood.foodmatch.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class JSONActivity extends AppCompatActivity {

    public static TextView textView;
    private static final String LOG_TAG = JSONActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json);

        Button button = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.textView);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new JSONTask().execute("http://api.collective-art.de/restaurants?token=n5DUfSC72hPABeEhu89Ex63soJ2oJCQfTxlim8MC6oHVLlrutMa3xDjDursL");
            }
        });
    }

    public static class JSONTask extends AsyncTask<String, String, String> {

        public interface OnJSONResultListener {
            void OnJSONResult(JSONArray resultArray);
        }

        // IntentService
        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);

                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {

                    buffer.append(line);
                }

                // check response code
                if (connection.getResponseCode() == 200) {

                    //
                    String finalJson = buffer.toString();
                    JSONArray parentArray = new JSONArray(finalJson);
                }

                JSONArray parentArray = null;
                JSONObject parentObject = null;
                String restaurantName;

                /*
                if (String.valueOf(finalJson.charAt(0)) == "[") {
                    Log.d(LOG_TAG, "its an array");
                    parentArray = new JSONArray(finalJson);
                } else {
                    parentObject = new JSONObject(finalJson);
                }
                */

                /*
                for (int i = 0; i < parentArray.length();i++) {

                    Log.d(LOG_TAG, " "+i);
                    JSONObject firstIndex = parentArray.getJSONObject(i);

                    restaurantName = firstIndex.getString("name");

                    Log.d(LOG_TAG, restaurantName);
                }
                */

                //Log.(LOG_TAG,"test" + finalJson);
                /*
                String restaurantName = parentObject.getString("name");

                JSONArray parentArray = parentObject.getJSONArray("dishes");

                JSONObject finalObject = parentArray.getJSONObject(0);

                String dishName = finalObject.getString("name");
                String dishId = finalObject.getString("_id");

                Log.d(LOG_TAG, restaurantName);

                return finalJson;
                */

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
        }
    }
}
