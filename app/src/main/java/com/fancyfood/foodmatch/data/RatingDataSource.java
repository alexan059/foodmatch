package com.fancyfood.foodmatch.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.fancyfood.foodmatch.data.migrations.RatingDbHelper;
import com.fancyfood.foodmatch.models.CardRating;

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
            RatingDbHelper.COLUMN_TIME
    };


    public RatingDataSource(Context context) {
        Log.d(LOG_TAG, "Unsere DataSource erzeugt jetzt den dbHelper.");
        dbHelper = new RatingDbHelper(context);
    }

    public void open() {
        Log.d(LOG_TAG, "Eine Referenz auf die Datenbank wird jetzt angefragt.");                    //open() and close() connection to the database
        database = dbHelper.getWritableDatabase();                                                  //getWritableDatabase();  open and write db
        Log.d(LOG_TAG, "Datenbank-Referenz erhalten. Pfad zur Datenbank: " + database.getPath());
    }

    public void close() {
        dbHelper.close();
        Log.d(LOG_TAG, "Datenbank mit Hilfe des DbHelpers geschlossen.");
    }

    public CardRating createRating(String dishID, boolean rating) {
        ContentValues values = new ContentValues();

        values.put(RatingDbHelper.COLUMN_DISH_ID, dishID);
        values.put(RatingDbHelper.COLUMN_RATING, rating);

        long insertId = database.insert(RatingDbHelper.TABLE_RATINGS, null, values);                    //write data in the database/ table

        Cursor cursor = database.query(RatingDbHelper.TABLE_RATINGS,columns, RatingDbHelper.COLUMN_ID + "=" + insertId,
                null, null, null, null);

        cursor.moveToFirst();
        CardRating ratingMemo = cursorToRatingMemo(cursor);
        cursor.close();

        return ratingMemo;
    }

    public List<CardRating> getAllRatingMemos() {                                                           //method to get/read all likes(favorites)
        List<CardRating> ratingMemoList = new ArrayList<>();

        Cursor cursor = database.query(RatingDbHelper.TABLE_RATINGS,
                columns, null, null, null, null, null);

        cursor.moveToFirst();
        CardRating ratingMemo;

        while(!cursor.isAfterLast()) {
            ratingMemo = cursorToRatingMemo(cursor);
            ratingMemoList.add(ratingMemo);
            Log.d(LOG_TAG, "Gericht ID: " + ratingMemo.getID());
            cursor.moveToNext();
        }

        cursor.close();

        return ratingMemoList;
    }

    private CardRating cursorToRatingMemo(Cursor cursor) {                                                  //method to convert cursor to card

        int idDishID = cursor.getColumnIndex(RatingDbHelper.COLUMN_DISH_ID);
        int idRating = cursor.getColumnIndex(RatingDbHelper.COLUMN_RATING);
        int idCreatedAt = cursor.getColumnIndex(RatingDbHelper.COLUMN_TIME);

        String id = cursor.getString(idDishID);
        boolean rating = (cursor.getInt(idRating) == 1);
        String createdAt = cursor.getString(idCreatedAt);

        Log.d(LOG_TAG, "gespeichert am: " + createdAt);

        return new CardRating(id,rating,createdAt);

    }

}
