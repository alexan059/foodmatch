package com.fancyfood.foodmatch.activities;

import android.os.Bundle;

import com.fancyfood.foodmatch.R;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateView(R.layout.activity_main);
    }
}
