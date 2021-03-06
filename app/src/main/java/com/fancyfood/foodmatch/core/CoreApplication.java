package com.fancyfood.foodmatch.core;

import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.support.multidex.MultiDex;
import android.support.v7.app.AlertDialog;

import com.fancyfood.foodmatch.helpers.GoogleApiLocationHelper;

/**
 * This class is used to share services and preferences to all activities inside
 * this project.
 */
public class CoreApplication extends Application {

    private static CoreApplication instance;
    private GoogleApiLocationHelper googleApiHelper;

    @Override
    public void onCreate() {
        super.onCreate();

        // Reference this instance to shared attribute
        instance = this;
        googleApiHelper = new GoogleApiLocationHelper(this);

    }

    /**
     * Handle firebase error on HTC One Mini (API 19)
     *
     * @param context
     */
    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }

    /**
     * This method is called every time, when an application needs global access
     * to preferences in all modules. It will return the current instance of
     * the running application.
     *
     * @return CoreApplication
     */
    public static synchronized CoreApplication getInstance() {
        return instance;
    }

    /**
     * Will return the GoogleApiHelper for connecting and disconnecting
     * the location service and setting up callback listener.
     *
     * @return GoogleApiLocationHelper
     */
    public static GoogleApiLocationHelper getGoogleApiHelper() {
        return getInstance().getGoogleApiHelperInstance();
    }

    public GoogleApiLocationHelper getGoogleApiHelperInstance() {
        return googleApiHelper;
    }
}
