package com.fancyfood.foodmatch.activities;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Carolina on 26.06.2016.
 */


public class LikeDbHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = LikeDbHelper.class.getSimpleName();

    public static final String DB_NAME = "favorites.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_LIKES = "likes";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DISH = "dish";
    public static final String COLUMN_LOCATION = "location";


    public static final String SQL_CREATE =
            "CREATE TABLE " + TABLE_LIKES +
                        "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + COLUMN_DISH + " TEXT NOT NULL, "
                            + COLUMN_LOCATION + " TEXT NOT NULL);";


    public LikeDbHelper(Context context) {                                                          //Create Database

        super(context, DB_NAME, null, DB_VERSION);
        Log.d(LOG_TAG, "DbHelper hat die Datenbank: " + getDatabaseName() + " erzeugt.");
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        try {
            Log.d(LOG_TAG, "Die Tabelle wird mit SQL-Befehl: " + SQL_CREATE + " angelegt.");        //Create Table
            db.execSQL(SQL_CREATE);
        }
        catch (Exception ex) {
            Log.e(LOG_TAG, "Fehler beim Anlegen der Tabelle: " + ex.getMessage());
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
