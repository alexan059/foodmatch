package com.fancyfood.foodmatch.data;

import android.provider.BaseColumns;

/**
 * This class provides all table names and sql queries for the sql helper class.
 * Read more: https://developer.android.com/training/basics/data-storage/databases.html
 */
public final class RatingsContract {

    private static final String INTEGER_TYPE = " INTEGER";
    private static final String VARCHAR_TYPE = " VARCHAR(255)";
    private static final String BOOLEAN_TYPE = " TINYINT(1)";
    private static final String DOUBLE_TYPE = " DOUBLE";
    private static final String TIMESTAMP_TYPE = " TIMESTAMP";

    private static final String NOT_NULL = " NOT NULL";
    private static final String COMMA_SEP = ",";

    // Prevent class to get instantiated by passing an empty constructor.
    public RatingsContract() {}

    /* Defines for table contents and queries */
    public static abstract class RatingEntry implements BaseColumns {
        public static final String TABLE_NAME = "ratings";

        public static final String COLUMN_NAME_DISH_ID = "dish_id";
        public static final String COLUMN_NAME_RATING = "rating";
        public static final String COLUMN_NAME_LAT = "lat";
        public static final String COLUMN_NAME_LNG = "lng";
        public static final String COLUMN_NAME_CREATED_AT = "created_at";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + RatingEntry.TABLE_NAME +
                        " (" + RatingEntry._ID + INTEGER_TYPE   + " PRIMARY KEY AUTOINCREMENT"  + COMMA_SEP
                        + RatingEntry.COLUMN_NAME_DISH_ID       + VARCHAR_TYPE  + NOT_NULL      + COMMA_SEP
                        + RatingEntry.COLUMN_NAME_RATING        + BOOLEAN_TYPE  + NOT_NULL      + COMMA_SEP
                        + RatingEntry.COLUMN_NAME_LAT           + DOUBLE_TYPE   + NOT_NULL      + COMMA_SEP
                        + RatingEntry.COLUMN_NAME_LNG           + DOUBLE_TYPE   + NOT_NULL      + COMMA_SEP
                        + RatingEntry.COLUMN_NAME_CREATED_AT    + TIMESTAMP_TYPE + " DEFAULT CURRENT_TIMESTAMP" + NOT_NULL
                        + " );";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + RatingEntry.TABLE_NAME;
    }
}
