package com.fancyfood.foodmatch.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.fancyfood.foodmatch.Constants;

public class CardReciever extends BroadcastReceiver {

    private static final String TAG = CardReciever.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String dish = intent.getStringExtra(Constants.BROADCAST_ACTION);
        //Log.d(TAG, dish);
    }
}
