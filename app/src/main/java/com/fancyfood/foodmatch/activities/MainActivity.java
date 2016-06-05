package com.fancyfood.foodmatch.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.fancyfood.foodmatch.R;

import static android.view.View.OnClickListener;

public class MainActivity extends BaseActivity implements OnClickListener, View.OnTouchListener
{

    private static final String EXTRA_METHOD = "com.fancyfood.foodmatch.METHOD";

    private boolean vibrate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateView(R.layout.activity_intro);

        final Button btHungry = (Button) findViewById(R.id.btHungry);
        final Button btDiscover = (Button) findViewById(R.id.btDiscover);

        if (btHungry != null && btDiscover != null) {
            btHungry.setOnTouchListener(this);
            btHungry.setOnClickListener(this);
            btDiscover.setOnTouchListener(this);
            btDiscover.setOnClickListener(this);
        }

    }

    @Override
    public void onClick(View v) {

        // Create new intent from start activity
        Intent intent = new Intent(this, MatchActivity.class);

        // Pass option parameter to define app mode
        switch (v.getId()) {
            case R.id.btHungry:
                intent.putExtra(EXTRA_METHOD, MatchActivity.FoodMethod.HUNGRY);
                break;
            case R.id.btDiscover:
                intent.putExtra(EXTRA_METHOD, MatchActivity.FoodMethod.DISCOVER);
                break;
            default:
                intent.putExtra(EXTRA_METHOD, MatchActivity.FoodMethod.HUNGRY);
        }

        startActivity(intent);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);

        if (action == MotionEvent.ACTION_DOWN && vibrate) {
            // Feedback on touch down
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            // Only vibrate once per touch
            vibrate = false;
        }

        // Reactivate vibration after release
        vibrate = (action == MotionEvent.ACTION_UP);

        return false;
    }
}
