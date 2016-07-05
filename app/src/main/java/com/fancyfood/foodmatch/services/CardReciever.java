package com.fancyfood.foodmatch.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.fancyfood.foodmatch.activities.MainActivity;
import com.fancyfood.foodmatch.preferences.Constants;

public class CardReciever extends BroadcastReceiver {

    private static final String TAG = CardReciever.class.getSimpleName();

    public interface OnDataReceiveListener {
        void onDataReceive();
    }

    private OnDataReceiveListener listener;

    public CardReciever(OnDataReceiveListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String status = intent.getStringExtra(Constants.EXTENDED_DATA_STATUS);

        switch (status) {
            case Constants.DATA_RECEIVED:
                Toast.makeText(context, "Ergebnisse erhalten.", Toast.LENGTH_SHORT).show();
                listener.onDataReceive();
                break;
            case Constants.DATA_NO_RESULTS:
                Toast.makeText(context, "Keine Ergebnisse.", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
