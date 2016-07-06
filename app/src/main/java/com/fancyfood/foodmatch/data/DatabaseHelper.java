package com.fancyfood.foodmatch.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "foodmatch.db";

    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        //context.deleteDatabase(DATABASE_NAME);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create ratings table
        db.execSQL(DishesContract.DishEntry.SQL_CREATE_ENTRIES);
        // Create ratings table
        db.execSQL(RatingsContract.RatingEntry.SQL_CREATE_ENTRIES);

        // Create update timestamp trigger
        db.execSQL(DishesContract.DishEntry.SQL_UPDATE_TIMESTAMP_TRIGGER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DishesContract.DishEntry.SQL_DELETE_ENTRIES);
        db.execSQL(RatingsContract.RatingEntry.SQL_DELETE_ENTRIES);
        onCreate(db);
        // Do nothing
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
        //super.onDowngrade(db, oldVersion, newVersion);
    }
}
