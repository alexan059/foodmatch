package com.fancyfood.foodmatch.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;

import com.fancyfood.foodmatch.models.Card;
import com.fancyfood.foodmatch.models.Rating;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static com.fancyfood.foodmatch.data.DishesContract.DishEntry.COLUMN_NAME_CREATED_AT;
import static com.fancyfood.foodmatch.data.DishesContract.DishEntry._ID;
import static com.fancyfood.foodmatch.data.DishesContract.DishEntry.COLUMN_NAME_CONSUMED;
import static com.fancyfood.foodmatch.data.DishesContract.DishEntry.COLUMN_NAME_DISH;
import static com.fancyfood.foodmatch.data.DishesContract.DishEntry.COLUMN_NAME_DISH_ID;
import static com.fancyfood.foodmatch.data.DishesContract.DishEntry.COLUMN_NAME_DISTANCE;
import static com.fancyfood.foodmatch.data.DishesContract.DishEntry.COLUMN_NAME_IMAGE_NAME;
import static com.fancyfood.foodmatch.data.DishesContract.DishEntry.COLUMN_NAME_LAT;
import static com.fancyfood.foodmatch.data.DishesContract.DishEntry.COLUMN_NAME_LNG;
import static com.fancyfood.foodmatch.data.DishesContract.DishEntry.COLUMN_NAME_LOCATION_NAME;
import static com.fancyfood.foodmatch.data.DishesContract.DishEntry.COLUMN_NAME_PRICING;
import static com.fancyfood.foodmatch.data.DishesContract.DishEntry.COLUMN_NAME_RESTAURANT_ID;
import static com.fancyfood.foodmatch.data.DishesContract.DishEntry.TABLE_NAME;

public class DishesDataSource {

    private static final String TAG = DishesDataSource.class.getSimpleName();

    Context context;

    SQLiteDatabase db;
    DatabaseHelper dbHelper;

    private String[] columns = {
            _ID,
            COLUMN_NAME_DISH_ID,
            COLUMN_NAME_RESTAURANT_ID,
            COLUMN_NAME_DISH,
            COLUMN_NAME_IMAGE_NAME,
            COLUMN_NAME_LOCATION_NAME,
            COLUMN_NAME_DISTANCE,
            COLUMN_NAME_PRICING,
            COLUMN_NAME_LAT,
            COLUMN_NAME_LNG,
            COLUMN_NAME_CONSUMED,
            COLUMN_NAME_CREATED_AT
    };

    public DishesDataSource(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }

    public void insertDish(Card card) {
        db = dbHelper.getWritableDatabase();

        // Create values
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_DISH_ID, card.getReference());
        values.put(COLUMN_NAME_RESTAURANT_ID, card.getLocationReference());
        values.put(COLUMN_NAME_DISH, card.getDish());
        values.put(COLUMN_NAME_IMAGE_NAME, card.getImageName());
        values.put(COLUMN_NAME_LOCATION_NAME, card.getLocationName());
        values.put(COLUMN_NAME_DISTANCE, card.getDistance());
        values.put(COLUMN_NAME_PRICING, card.getPricing());
        values.put(COLUMN_NAME_LAT, card.getLocation().getLatitude());
        values.put(COLUMN_NAME_LNG, card.getLocation().getLongitude());
        values.put(COLUMN_NAME_CONSUMED, 0);

        // Insert values
        db.insert(TABLE_NAME, null, values);

        // Close db connection
        db.close();
    }

    public void getAllDishes() {
        db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, null);
        cursor.moveToFirst();

        //long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(_ID));

        String dishId = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_DISH_ID));
        String dish = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_DISH));
        double lat = cursor.getDouble(cursor.getColumnIndex(COLUMN_NAME_LAT));
        double lng = cursor.getDouble(cursor.getColumnIndex(COLUMN_NAME_LNG));

        Log.d(TAG, dishId + " " + dish + " " + Double.toString(lat) + " " + Double.toString(lng));

        cursor.close();
        db.close();
    }

    public Card getFirstData() {
        db = dbHelper.getReadableDatabase();

        Cursor cursor =  db.query(TABLE_NAME, columns, null, null, null, null, null);
        cursor.moveToFirst();

        String dishId = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_DISH_ID));
        String dish = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_DISH));
        String imageName = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_IMAGE_NAME));

        Drawable image = loadImage(imageName);

        double lat = cursor.getDouble(cursor.getColumnIndex(COLUMN_NAME_LAT));
        double lng = cursor.getDouble(cursor.getColumnIndex(COLUMN_NAME_LNG));

        Location location = new Location("");
        location.setLatitude(lat);
        location.setLongitude(lng);

        String locationName = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_LOCATION_NAME));
        int distance = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_DISTANCE));
        int pricing = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_PRICING));

        cursor.close();
        db.close();

        return new Card(dishId, location, image, dish, locationName, distance, pricing);
    }

    private Drawable loadImage(String imageName) {
        // Get application context wrapper
        ContextWrapper contextWrapper = new ContextWrapper(context.getApplicationContext());
        // Path to app data
        File directory = contextWrapper.getDir("images", Context.MODE_PRIVATE);

        Drawable image = null;

        try {
            File imageFile = new File(directory, imageName);
            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(imageFile));

            image = new BitmapDrawable(context.getResources(), bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return image;
    }

//    public List<Card> listDishes() {
//        List<Card> cardMemoList = new ArrayList<>();
//        db = dbHelper.getReadableDatabase();
//
//        Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, null);
//
//        cursor.moveToFirst();
//        Card cardMemo;
//
//        while(!cursor.isAfterLast()) {
//            cardMemo = cursorToCardMemo(cursor);
//            ratingMemoList.add(ratingMemo);
//            Log.d(TAG, "Gericht ID: " + ratingMemo.getReference());
//            cursor.moveToNext();
//        }
//
//        cursor.close();
//
//        return ratingMemoList;
//    }

//    private Rating cursorCardMemo(Cursor cursor) {
//
//        int idDishID = cursor.getColumnIndex(COLUMN_NAME_DISH_ID);
//        int idRating = cursor.getColumnIndex(COLUMN_NAME_DISH_ID);
//        int idCreatedAt = cursor.getColumnIndex(COLUMN_NAME_CREATED_AT);
//        int idLat = cursor.getColumnIndex(COLUMN_NAME_LAT);
//        int idLng = cursor.getColumnIndex(COLUMN_NAME_LNG);
//        int idDishID = cursor.getColumnIndex(COLUMN_NAME_DISH_ID);
//        int idRating = cursor.getColumnIndex(COLUMN_NAME_DISH_ID);
//        int idCreatedAt = cursor.getColumnIndex(COLUMN_NAME_CREATED_AT);
//        int idLat = cursor.getColumnIndex(COLUMN_NAME_LAT);
//        int idLng = cursor.getColumnIndex(COLUMN_NAME_LNG);
//
//        String id = cursor.getString(idDishID);
//        boolean rating = (cursor.getInt(idRating) == 1);
//        Location location = new Location("");
//        location.setLatitude(cursor.getFloat(idLat));
//        location.setLongitude(cursor.getFloat(idLng));
//        String createdAt = cursor.getString(idCreatedAt);
//
//        Log.d(TAG, "gespeichert am: " + createdAt);
//
//        return new Rating(id,rating,location,createdAt);
//
//        JSONArray dishes = restaurant.getJSONArray("dishes");
//        JSONObject dish = dishes.getJSONObject(0);
//        String _id = dish.getString("_id");
//        String name = dish.getString("name");
//        String locationName = restaurant.getString("name");
//
//        JSONObject geo = restaurant.getJSONObject("geo");
//        JSONArray coordinates = geo.getJSONArray("coordinates");
//        Location location = new Location("");
//        location.setLatitude(coordinates.getDouble(0));
//        location.setLongitude(coordinates.getDouble(1));
//
//        JSONObject media = dish.getJSONObject("media");
//        String fileName = media.getString("file");
//        URL imageUrl = getFullUrl("restaurants/media/" + fileName);
//
//        HttpConnectionHelper.downloadImage(this, imageUrl, fileName);
//
//        //Drawable image = loadImage(directory, fileName);
//
//        Card card = new Card(_id, location, null, name, locationName, 0, 0);
//        card.setImageName(fileName);
//
//        return card;
//
//    }

}
