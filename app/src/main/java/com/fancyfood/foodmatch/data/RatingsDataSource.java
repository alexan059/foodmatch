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

/**
 * Created by Carolina on 26.06.2016.
 */
public class RatingsDataSource {

    private static final String LOG_TAG = RatingsDataSource.class.getSimpleName();

    private SQLiteDatabase database;
    private RatingsDbHelper dbHelper;

    private String[] columns = {
            RatingsContract.RatingEntry._ID,
            RatingsContract.RatingEntry.COLUMN_NAME_DISH_ID,
            RatingsContract.RatingEntry.COLUMN_NAME_RATING,
            RatingsContract.RatingEntry.COLUMN_NAME_LAT,
            RatingsContract.RatingEntry.COLUMN_NAME_LNG,
            RatingsContract.RatingEntry.COLUMN_NAME_CREATED_AT
    };


    public RatingsDataSource(Context context) {
        Log.d(LOG_TAG, "Unsere DataSource erzeugt jetzt den dbHelper.");
        dbHelper = new RatingsDbHelper(context);
    }

    public void open() {
        //open() and close() connection to the database
        Log.d(LOG_TAG, "Eine Referenz auf die Datenbank wird jetzt angefragt.");
        //getWritableDatabase();  open and write db
        database = dbHelper.getWritableDatabase();
        Log.d(LOG_TAG, "Datenbank-Referenz erhalten. Pfad zur Datenbank: " + database.getPath());
    }

    public void close() {
        dbHelper.close();
        Log.d(LOG_TAG, "Datenbank mit Hilfe des DbHelpers geschlossen.");
    }

    public Rating createRating(Rating rating) {
        ContentValues values = new ContentValues();

        values.put(RatingsContract.RatingEntry.COLUMN_NAME_DISH_ID, rating.getReference());
        values.put(RatingsContract.RatingEntry.COLUMN_NAME_RATING, rating.getRating());
        values.put(RatingsContract.RatingEntry.COLUMN_NAME_LAT, rating.getLocation().getLatitude());
        values.put(RatingsContract.RatingEntry.COLUMN_NAME_LNG, rating.getLocation().getLongitude());

        //write data in the database/ table
        long insertId = database.insert(RatingsContract.RatingEntry.TABLE_NAME, null, values);

        Cursor cursor = database.query(RatingsContract.RatingEntry.TABLE_NAME,columns,
                RatingsContract.RatingEntry._ID + "=" + insertId, null, null, null, null);

        cursor.moveToFirst();
        Rating ratingMemo = cursorToRatingMemo(cursor);
        cursor.close();

        return ratingMemo;
    }

    //method to get/read all likes(favorites)
    public List<Rating> getAllRatingMemos() {
        List<Rating> ratingMemoList = new ArrayList<Rating>();

        Cursor cursor = database.query(RatingsContract.RatingEntry.TABLE_NAME,
                columns, null, null, null, null, null);

        cursor.moveToFirst();
        Rating ratingMemo;

        while(!cursor.isAfterLast()) {
            ratingMemo = cursorToRatingMemo(cursor);
            ratingMemoList.add(ratingMemo);
            Log.d(LOG_TAG, "Gericht ID: " + ratingMemo.getReference());
            cursor.moveToNext();
        }

        cursor.close();

        return ratingMemoList;
    }

    //method to convert cursor to card
    private Rating cursorToRatingMemo(Cursor cursor) {

        int idDishID = cursor.getColumnIndex(RatingsContract.RatingEntry.COLUMN_NAME_DISH_ID);
        int idRating = cursor.getColumnIndex(RatingsContract.RatingEntry.COLUMN_NAME_DISH_ID);
        int idCreatedAt = cursor.getColumnIndex(RatingsContract.RatingEntry.COLUMN_NAME_CREATED_AT);
        int idLat = cursor.getColumnIndex(RatingsContract.RatingEntry.COLUMN_NAME_LAT);
        int idLng = cursor.getColumnIndex(RatingsContract.RatingEntry.COLUMN_NAME_LNG);

        String id = cursor.getString(idDishID);
        boolean rating = (cursor.getInt(idRating) == 1);
        Location location = new Location("");
        location.setLatitude(cursor.getFloat(idLat));
        location.setLongitude(cursor.getFloat(idLng));
        String createdAt = cursor.getString(idCreatedAt);

        Log.d(LOG_TAG, "gespeichert am: " + createdAt);

        return new Rating(id,rating,location,createdAt);

    }

}
