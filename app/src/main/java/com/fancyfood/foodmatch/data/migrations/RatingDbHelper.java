package com.fancyfood.foodmatch.data.migrations;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Carolina on 26.06.2016.
 */


public class RatingDbHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = RatingDbHelper.class.getSimpleName();

    public static final String DB_NAME = "foodmatch.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_RATINGS = "ratings";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DISH_ID = "dish_id";
    public static final String COLUMN_RATING = "rating";
    public static final String COLUMN_TIME = "created_at";


    public static final String SQL_CREATE =
            "CREATE TABLE " + TABLE_RATINGS +
                        "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + COLUMN_DISH_ID + " VARCHAR(255) NOT NULL, "
                            + COLUMN_RATING + " TINYINT(1) NOT NULL, "
                            + COLUMN_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL);";


    public RatingDbHelper(Context context) {                                                          //Create Database
        super(context, DB_NAME, null, DB_VERSION);
        Log.d(LOG_TAG, "DbHelper hat die Datenbank: " + getDatabaseName() + " erzeugt.");
        context.deleteDatabase(DB_NAME);
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
