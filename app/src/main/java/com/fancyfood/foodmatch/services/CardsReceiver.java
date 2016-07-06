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
            case Constants.DATA_PARSE_ERROR:
                Log.d(TAG, "Parse Error - JSON Data Format not valid.");
            case Constants.DATA_NO_RESULTS:
                Toast.makeText(context, "Keine Ergebnisse", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
