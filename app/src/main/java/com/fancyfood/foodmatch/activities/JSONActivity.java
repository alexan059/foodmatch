package com.fancyfood.foodmatch.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class JSONActivity extends AppCompatActivity {

    public TextView textView;
    public ImageView imageView;

    public String token = "n5DUfSC72hPABeEhu89Ex63soJ2oJCQfTxlim8MC6oHVLlrutMa3xDjDursL";
    public String url = "http://api.collective-art.de/";

    private static final String LOG_TAG = JSONActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json);

        Button button = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.textView);
        imageView = (ImageView)findViewById(R.id.imageView);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new JSONTask().execute(url + "restaurants?token=" + token);
            }
        });
    }

    public class JSONTask extends AsyncTask<String, String, String> {

        public Bitmap bitmap = null;

        // IntentService
        protected String getFullUrl(String uri) {

            return url  + uri + "?token=" + token;
        }

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;


            try {
                URL url = new URL(params[0]);

                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                if(stream != null) {

                    reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuffer buffer = new StringBuffer();
                    String line = "";

                    while ((line = reader.readLine()) != null) {

                        buffer.append(line);
                    }

                    // check response code for 200
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                        JSONObject parentObject = null;
                        JSONArray parentArray = null;
                        JSONObject finalObject = null;
                        JSONArray finalArray = null;

                        Log.d(LOG_TAG, "DIR: " + Environment.getExternalStorageDirectory());
                        String finalJson = buffer.toString();
                        parentArray = new JSONArray(finalJson);

                        for(int i = 0;i < parentArray.length(); i++) {

                            finalObject = parentArray.getJSONObject(i).getJSONObject("media");
                            String fileName = finalObject.getString("file");

                            Log.d(LOG_TAG, fileName);
                            Log.d(LOG_TAG, getFullUrl("restaurants/media/" + fileName));

                            URL imageurl = new URL(getFullUrl("restaurants/media/" + fileName));
                            bitmap = BitmapFactory.decodeStream(imageurl.openConnection().getInputStream());

                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

                            File file = new File(Environment.getExternalStorageDirectory() + File.separator + fileName);
                            file.createNewFile();

                            FileOutputStream fileOutput = new FileOutputStream(file);

                            fileOutput.write(bytes.toByteArray());
                            fileOutput.close();
                        }
                        return finalJson;
                    }

                }


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
            imageView.setImageBitmap(bitmap);
        }
    }
}
