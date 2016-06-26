package com.fancyfood.foodmatch.activities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.fancyfood.foodmatch.models.Card;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Carolina on 26.06.2016.
 */
public class LikeDataSource {

    private static final String LOG_TAG = LikeDataSource.class.getSimpleName();

    private SQLiteDatabase database;
    private LikeDbHelper dbHelper;

    private String[] columns = {
            LikeDbHelper.COLUMN_ID,
            LikeDbHelper.COLUMN_DISH,
            LikeDbHelper.COLUMN_LOCATION
    };


    public LikeDataSource(Context context) {
        Log.d(LOG_TAG, "Unsere DataSource erzeugt jetzt den dbHelper.");
        dbHelper = new LikeDbHelper(context);
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

    public Card createCard(String dish, String location) {
        ContentValues values = new ContentValues();
        values.put(LikeDbHelper.COLUMN_DISH, dish);
        values.put(LikeDbHelper.COLUMN_LOCATION, location);

        long insertId = database.insert(LikeDbHelper.TABLE_LIKES, null, values);                    //write data in the database/ table

        Cursor cursor = database.query(LikeDbHelper.TABLE_LIKES,columns, LikeDbHelper.COLUMN_ID + "=" + insertId,
                null, null, null, null);

        cursor.moveToFirst();
        Card cardMemo = cursorToCardMemo(cursor);
        cursor.close();

        return cardMemo;
    }

    public List<Card> getAllCardMemos() {                                                           //method to get/read all likes(favorites)
        List<Card> cardMemoList = new ArrayList<>();

        Cursor cursor = database.query(LikeDbHelper.TABLE_LIKES,
                columns, null, null, null, null, null);

        cursor.moveToFirst();
        Card cardMemo;

        while(!cursor.isAfterLast()) {
            cardMemo = cursorToCardMemo(cursor);
            cardMemoList.add(cardMemo);
            Log.d(LOG_TAG, "Inhalt: " + cardMemo.toString());
            cursor.moveToNext();
        }

        cursor.close();

        return cardMemoList;
    }


    private Card cursorToCardMemo(Cursor cursor) {                                                  //method to convert cursor to card
        //int idIndex = cursor.getColumnIndex(LikeDbHelper.COLUMN_ID);                              //there is not ID in Card.java
        int idDish = cursor.getColumnIndex(LikeDbHelper.COLUMN_DISH);
        int idLocation = cursor.getColumnIndex(LikeDbHelper.COLUMN_LOCATION);

        String dish = cursor.getString(idDish);
        String location = cursor.getString(idLocation);
        //long id = cursor.getLong(idIndex);

        Card cardMemo = new Card(null,dish,location,0,0);

        return cardMemo;
    }

}
