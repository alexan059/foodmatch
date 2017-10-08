package com.fancyfood.foodmatch.preferences;

import android.content.Intent;
import android.content.res.Resources;

import com.fancyfood.foodmatch.R;
import com.fancyfood.foodmatch.core.CoreApplication;

public final class Constants {
    public static final String BROADCAST_ACTION = "com.fancyfood.foodmatch.DATA_BROADCAST";
    public static final String BROADCAST_STATUS = "com.fancyfood.foodmatch.STATUS_BROADCAST";
    public static final String BROADCAST_TOKEN = "com.fancyfood.foodmatch.TOKEN_BROADCAST";
    public static final String EXTENDED_DATA_STATUS = "com.fancyfood.foodmatch.STATUS";
    public static final String EXTENDED_DATA_TOKEN = "com.fancyfood.foodmatch.TOKEN";
    public static final String TOKEN_RECEIVED = "com.fancyfood.foodmatch.TOKEN_RECEIVED";
    public static final String DATA_RECEIVED = "com.fancyfood.foodmatch.RECEIVED";
    public static final String DATA_NO_RESULTS = "com.fancyfood.foodmatch.NO_RESULTS";
    public static final String DATA_PARSE_ERROR = "com.fancyfood.foodmatch.PARSE_ERROR";
    public static final String DATA_RESTART_SERVICE = "com.fancyfood.foodmatch.RESTART_SERVICE";

    // Sync Service Code
    public static final int SYNC_SERVICE = 555777999;

    // Status for StatusService
    public static final String GPS_ENABLED = "com.fancyfood.foodmatch.GPS_ENABLED";
    public static final String GPS_DISBALED = "com.fancyfood.foodmatch.GPS_DISABLED";


    // TODO timestamp format for secret 2016-07-06 10:13:56
    public static final String API_ENTRY_POINT = CoreApplication.getInstance().getApplicationContext().getString(R.string.api_entry_point);
    public static final String API_SECRET = CoreApplication.getInstance().getApplicationContext().getString(R.string.api_secret);
    public static final int API_VERSION = Integer.parseInt(CoreApplication.getInstance().getApplicationContext().getString(R.string.api_version));
}
