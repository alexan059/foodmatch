package com.fancyfood.foodmatch.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import com.fancyfood.foodmatch.data.migrations.RatingDbHelper;
import com.fancyfood.foodmatch.models.Rating;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Carolina on 26.06.2016.
 */
public class RatingDataSource {

    private static final String LOG_TAG = RatingDataSource.class.getSimpleName();

    private SQLiteDatabase database;
    private RatingDbHelper dbHelper;

    private String[] columns = {
            RatingDbHelper.COLUMN_ID,
            RatingDbHelper.COLUMN_DISH_ID,
            RatingDbHelper.COLUMN_RATING,
            RatingDbHelper.COLUMN_TIMESTAMP,
            RatingDbHelper.COLUMN_LAT,
            RatingDbHelper.COLUMN_LNG
    };


    public RatingDataSource(Context context) {
        Log.d(LOG_TAG, "Unsere DataSource erzeugt jetzt den dbHelper.");
        dbHelper = new RatingDbHelper(context);
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

        values.put(RatingDbHelper.COLUMN_DISH_ID, rating.getID());
        values.put(RatingDbHelper.COLUMN_RATING, rating.getRating());
        values.put(RatingDbHelper.COLUMN_LAT, rating.getLocation().getLatitude());
        values.put(RatingDbHelper.COLUMN_LNG, rating.getLocation().getLongitude());

        //write data in the database/ table
        long insertId = database.insert(RatingDbHelper.TABLE_RATINGS, null, values);

        Cursor cursor = database.query(RatingDbHelper.TABLE_RATINGS,columns, RatingDbHelper.COLUMN_ID + "=" + insertId,
                null, null, null, null);

        cursor.moveToFirst();
        Rating ratingMemo = cursorToRatingMemo(cursor);
        cursor.close();

        return ratingMemo;
    }

    //method to get/read all likes(favorites)
    public List<Rating> getAllRatingMemos() {
        List<Rating> ratingMemoList = new ArrayList<Rating>();

        Cursor cursor = database.query(RatingDbHelper.TABLE_RATINGS,
                columns, null, null, null, null, null);

        cursor.moveToFirst();
        Rating ratingMemo;

        while(!cursor.isAfterLast()) {
            ratingMemo = cursorToRatingMemo(cursor);
            ratingMemoList.add(ratingMemo);
            Log.d(LOG_TAG, "Gericht ID: " + ratingMemo.getID());
            cursor.moveToNext();
        }

        cursor.close();

        return ratingMemoList;
    }

    //method to convert cursor to card
    private Rating cursorToRatingMemo(Cursor cursor) {

        int idDishID = cursor.getColumnIndex(RatingDbHelper.COLUMN_DISH_ID);
        int idRating = cursor.getColumnIndex(RatingDbHelper.COLUMN_RATING);
        int idCreatedAt = cursor.getColumnIndex(RatingDbHelper.COLUMN_TIMESTAMP);
        int idLat = cursor.getColumnIndex(RatingDbHelper.COLUMN_LAT);
        int idLng = cursor.getColumnIndex(RatingDbHelper.COLUMN_LNG);

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
