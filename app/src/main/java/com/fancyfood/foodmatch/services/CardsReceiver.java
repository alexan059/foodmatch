package com.fancyfood.foodmatch.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.fancyfood.foodmatch.preferences.Constants;

public class CardsReceiver extends BroadcastReceiver {

    private static final String TAG = CardsReceiver.class.getSimpleName();

    public interface OnDataReceiveListener {
        void onDataReceive();
        void onNoResult();
        void onRestartService();
    }

    private OnDataReceiveListener listener;

    public CardsReceiver(OnDataReceiveListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String status = intent.getStringExtra(Constants.EXTENDED_DATA_STATUS);

        switch (status) {
            case Constants.DATA_RECEIVED:
                listener.onDataReceive();
                break;
            case Constants.DATA_NO_RESULTS:
                listener.onNoResult();
                break;
            case Constants.DATA_RESTART_SERVICE:
                listener.onRestartService();
                break;
            case Constants.DATA_PARSE_ERROR:
                Toast.makeText(context, "Es ist ein Fehler aufgetreten", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Parse Error - JSON Data Format not valid.");
                break;
        }
    }
}
