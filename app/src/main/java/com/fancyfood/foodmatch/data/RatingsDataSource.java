package com.fancyfood.foodmatch.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import com.fancyfood.foodmatch.models.Rating;

import java.util.ArrayList;
import java.util.List;

import static com.fancyfood.foodmatch.data.RatingsContract.RatingEntry.TABLE_NAME;
import static com.fancyfood.foodmatch.data.RatingsContract.RatingEntry._ID;
import static com.fancyfood.foodmatch.data.RatingsContract.RatingEntry.COLUMN_NAME_CREATED_AT;
import static com.fancyfood.foodmatch.data.RatingsContract.RatingEntry.COLUMN_NAME_DISH_ID;
import static com.fancyfood.foodmatch.data.RatingsContract.RatingEntry.COLUMN_NAME_LAT;
import static com.fancyfood.foodmatch.data.RatingsContract.RatingEntry.COLUMN_NAME_LNG;
import static com.fancyfood.foodmatch.data.RatingsContract.RatingEntry.COLUMN_NAME_RATING;

public class RatingsDataSource {

    private static final String TAG = RatingsDataSource.class.getSimpleName();

    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    private String[] columns = {
            _ID,
            COLUMN_NAME_DISH_ID,
            COLUMN_NAME_RATING,
            COLUMN_NAME_LAT,
            COLUMN_NAME_LNG,
            COLUMN_NAME_CREATED_AT
    };


    public RatingsDataSource(Context context) {
        Log.d(TAG, "Unsere DataSource erzeugt jetzt den dbHelper.");
        dbHelper = new DatabaseHelper(context);
    }

    public Rating createRating(Rating rating) {
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME_DISH_ID, rating.getReference());
        values.put(COLUMN_NAME_RATING, rating.getRating());
        values.put(COLUMN_NAME_LAT, rating.getLocation().getLatitude());
        values.put(COLUMN_NAME_LNG, rating.getLocation().getLongitude());

        //write data in the db/ table
        long insertId = db.insert(TABLE_NAME, null, values);

        Cursor cursor = db.query(TABLE_NAME,columns,
                _ID + "=" + insertId, null, null, null, null);

        cursor.moveToFirst();
        Rating ratingMemo = cursorToRatingMemo(cursor);
        cursor.close();
        db.close();

        return ratingMemo;
    }

    public ArrayList<Rating> getNewRatings(String timestamp) {
        db = dbHelper.getReadableDatabase();

        String selection = COLUMN_NAME_CREATED_AT + " > ?";
        String[] selectionArgs = { timestamp };

        Cursor cursor = db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        cursor.moveToFirst();

        Rating rating;
        ArrayList<Rating> ratingsList = new ArrayList<>();

        while(!cursor.isAfterLast()) {
            rating = cursorToRatingMemo(cursor);
            ratingsList.add(rating);
            cursor.moveToNext();
        }

        cursor.close();
        db.close();

        return ratingsList;
    }

    public int getRatingsCount(String timestamp) {
        db = dbHelper.getReadableDatabase();

        String selection = COLUMN_NAME_CREATED_AT + " > ?";
        String[] selectionArgs = { timestamp };

        Cursor cursor = db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null);

        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    //method to get/read all likes(favorites)
    public List<Rating> getAllRatingMemos() {
        db = dbHelper.getReadableDatabase();
        List<Rating> ratingMemoList = new ArrayList<Rating>();

        Cursor cursor = db.query(TABLE_NAME,
                columns, null, null, null, null, null);

        cursor.moveToFirst();
        Rating ratingMemo;

        while(!cursor.isAfterLast()) {
            ratingMemo = cursorToRatingMemo(cursor);
            ratingMemoList.add(ratingMemo);
            Log.d(TAG, "Gericht ID: " + ratingMemo.getReference());
            cursor.moveToNext();
        }

        cursor.close();
        db.close();

        return ratingMemoList;
    }

    //method to convert cursor to card
    private Rating cursorToRatingMemo(Cursor cursor) {

        int idDishID = cursor.getColumnIndex(COLUMN_NAME_DISH_ID);
        int idRating = cursor.getColumnIndex(COLUMN_NAME_DISH_ID);
        int idCreatedAt = cursor.getColumnIndex(COLUMN_NAME_CREATED_AT);
        int idLat = cursor.getColumnIndex(COLUMN_NAME_LAT);
        int idLng = cursor.getColumnIndex(COLUMN_NAME_LNG);

        String id = cursor.getString(idDishID);
        boolean rating = (cursor.getInt(idRating) == 1);
        Location location = new Location("");
        location.setLatitude(cursor.getFloat(idLat));
        location.setLongitude(cursor.getFloat(idLng));
        String createdAt = cursor.getString(idCreatedAt);

        Log.d(TAG, "gespeichert am: " + createdAt);

        return new Rating(id,rating,location,createdAt);

    }

}
