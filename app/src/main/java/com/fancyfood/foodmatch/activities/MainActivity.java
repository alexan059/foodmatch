package com.fancyfood.foodmatch.activities;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.fancyfood.foodmatch.fragments.NoResultDialogFragment;
import com.fancyfood.foodmatch.fragments.NoResultDialogFragment.NoResultDialogListener;
import com.fancyfood.foodmatch.helpers.DataSourceHelper;
import com.fancyfood.foodmatch.modules.SwipeCards;
import com.fancyfood.foodmatch.modules.SwipeCards.OnFlingCallbackListener;
import com.fancyfood.foodmatch.preferences.Constants;
import com.fancyfood.foodmatch.core.CoreActivity;
import com.fancyfood.foodmatch.core.CoreApplication;
import com.fancyfood.foodmatch.R;
import com.fancyfood.foodmatch.fragments.RadiusDialogFragment;
import com.fancyfood.foodmatch.fragments.RadiusDialogFragment.RadiusDialogListener;
import com.fancyfood.foodmatch.helpers.GoogleApiLocationHelper;
import com.fancyfood.foodmatch.helpers.GoogleApiLocationHelper.OnLocationChangedListener;
import com.fancyfood.foodmatch.models.Card;
import com.fancyfood.foodmatch.preferences.Preferences;
import com.fancyfood.foodmatch.services.CardsReceiver;
import com.fancyfood.foodmatch.services.CardsPullService;
import com.fancyfood.foodmatch.services.CardsReceiver.OnDataReceiveListener;
import com.fancyfood.foodmatch.services.RatingsPushService;
import com.fancyfood.foodmatch.services.StatusReceiver;
import com.fancyfood.foodmatch.services.TokenReceiver;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;

import static android.view.View.OnClickListener;
import static android.view.View.OnTouchListener;

public class MainActivity extends CoreActivity implements OnClickListener, OnTouchListener,
        OnLocationChangedListener, RadiusDialogListener, OnDataReceiveListener,
        OnFlingCallbackListener, NoResultDialogListener {

    // Debug Tag
    private static final String TAG = MainActivity.class.getSimpleName();

    // Database connection
    DataSourceHelper database;

    // Views
    private SwitchCompat modeSwitch;
    private ProgressBar progressBar;
    private SwipeCards swipeCards;
    private Button btPositive;
    private Button btNegative;

    // Location and radius settings
    private GoogleApiLocationHelper locationHelper;
    private Location currentLocation;
    private int radius;

    // Flags
    private boolean eatMode = false;
    private boolean switchTouched = false;
    private boolean isStarted = false;
    private boolean isCollapsed = false;
    private boolean isLocated = false;

    /* Activity Lifecycle */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateView(R.layout.activity_main);

        // Initialize all buttons, menus and switches
        initializeUserActionListener();

        // Restore preferences
        radius = Preferences.restoreRadius(this);

        // Get database helper
        database = new DataSourceHelper(this);

        // Start on location change listener
        if (CoreApplication.getGoogleApiHelper() != null) {
            locationHelper = CoreApplication.getGoogleApiHelper();
            locationHelper.setOnLocationChangedListener(this);
        }

        // Initialize cards container
        swipeCards = new SwipeCards(this, (SwipeFlingAdapterView) findViewById(R.id.rating_cards));
        swipeCards.setOnFlingCallbackListener(this);

        // Set intent filter for receiving data
        setIntentFilter();

        Log.d(TAG, "onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Connect the location helper
        if (locationHelper != null && !locationHelper.isConnected()) {
            locationHelper.connect();
        }

        startSyncService();

        Log.d(TAG, "onStart");

    }

    @Override
    protected void onStop() {
        super.onStop();

        // Empty dishes data base for next session
        database.truncateDishes();

        // Disconnect location helper
        if (locationHelper != null && locationHelper.isConnected()) {
            locationHelper.disconnect();
        }

        // Debug only
        //Preferences.storeToken(this, null);

        // Store preferences if changed
        Preferences.storeRadius(this, radius);

        stopSyncService();

        Log.d(TAG, "onStop");
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Disconnect location helper
        if (locationHelper != null && locationHelper.isConnected()) {
            locationHelper.disconnect();
        }

        // Store preferences if changed
        Preferences.storeRadius(this, radius);

        Log.d(TAG, "onPause");
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        // Connect the location helper
        if (locationHelper != null && !locationHelper.isConnected()) {
            locationHelper.connect();
        }

        Log.d(TAG, "onPostResume");
    }

    /* Sync Service */

    private void startSyncService() {
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent intent = new Intent(this, RatingsPushService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, Constants.SYNC_SERVICE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 60000 * 5, pendingIntent);
    }

    private void stopSyncService() {
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent intent = new Intent(this, RatingsPushService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, Constants.SYNC_SERVICE, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        manager.cancel(pendingIntent);
    }

    /* IntentService for receiving data */

    private void firstStart() {
        if (!isStarted) {
            startDataService();
            isStarted = true;
        }
    }

    private void startDataService() {
        // "restaurants/55.56/57.6/2000" -> resource/lat/lng/radius
        String lat = Double.toString(currentLocation.getLatitude());
        String lng = Double.toString(currentLocation.getLongitude());
        String rad = Integer.toString(radius) + "00";

        String uri = "restaurants/" + lng + "/" + lat + "/" + rad;

        Intent intent = new Intent(this, CardsPullService.class);
        intent.setData(Uri.parse(uri));

        startService(intent);
    }

    @Override
    public void onDataReceive() {
        ArrayList<Card> cards = database.getCurrentCards(10);

        if (cards.size() > 0) {
            swipeCards.appendCards(cards);
            btNegative.setEnabled(true);
            btPositive.setEnabled(true);
        } else {
            onNoResult();
        }

    }

    @Override
    public void onNoResult() {
        NoResultDialogFragment dialog = new NoResultDialogFragment();
        dialog.show(getFragmentManager(), NoResultDialogFragment.class.getSimpleName());
    }

    @Override
    public void onRestartService() {
        startDataService();
    }

    @Override
    public void requestCards() {
        startDataService();
    }

    @Override
    public void disableButtons() {
        btNegative.setEnabled(false);
        btPositive.setEnabled(false);
    }

    /* Google Api Helper and Location Listener */

    @Override
    public void startMapsActivity(Card card) {
        if (eatMode) {
            // Create new intent for MapsActivity
            Intent i = new Intent(MainActivity.this, MapsActivity.class);

            // Get location from card
            double lat = card.getLocation().getLatitude();
            double lng = card.getLocation().getLongitude();

            // Put coordinates as extra data for MapsActivity
            i.putExtra("Lat", lat);
            i.putExtra("Lng", lng);

            Log.d(TAG, "Starting Maps with LAT: " + Double.toString(lat) + ", LNG: " + Double.toString(lng));

            // Start MapsActivity
            startActivity(i);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;

        if (!isLocated) {
            displayUI();
            if (isCollapsed) firstStart();
            progressBar.setVisibility(View.INVISIBLE);
            isLocated = true;
        }

        Log.d(TAG, "Position: LAT " + Double.toString(location.getLatitude()) + "| LNG " + Double.toString(location.getLongitude()));
    }

    @Override
    public void showPermissionDialog() {
        ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, GoogleApiLocationHelper.MY_PERMISSIONS_COARSE_LOCATIONS);
        ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, GoogleApiLocationHelper.MY_PERMISSIONS_FINE_LOCATIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Handle permission requests
        switch (requestCode) {
            case GoogleApiLocationHelper.MY_PERMISSIONS_COARSE_LOCATIONS:
            case GoogleApiLocationHelper.MY_PERMISSIONS_FINE_LOCATIONS:
                // Permission granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationHelper.startDelayedService();
                }
                // Permission denied
                else {
                    Toast.makeText(this, "Es kann kein Standort abgerufen werden!", Toast.LENGTH_SHORT).show();
                }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onGpsDisabled() {
        GoogleApiLocationHelper.isGpsEnabled(this, true, true);
    }

    /* Helper methods */

    public void initializeUserActionListener() {
        // Progress bar
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        // Set navigation
        getNavigation().setNavigationItemSelectedListener(new OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.nav_radius:
                        RadiusDialogFragment dialog = new RadiusDialogFragment();
                        dialog.setRadius(radius);
                        dialog.show(getFragmentManager(), RadiusDialogFragment.class.getSimpleName());
                        break;
                    //case R.id.nav_settings:
                    //    break;
                }

                return false;
            }
        });

        // Get Switch Compat
        modeSwitch = (SwitchCompat) getMenu().findItem(R.id.nav_switch)
                .getActionView().findViewById(R.id.switch_compat);
        modeSwitch.setOnTouchListener(this);
        modeSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (switchTouched) {
                    switchTouched = false;
                    eatMode = isChecked;

                    Toast.makeText(getApplicationContext(), "\"Sofort essen\" wurde " + (!isChecked ? "de":"") + "aktiviert.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set buttons
        final Button btHungry = (Button) findViewById(R.id.btHungry);
        final Button btDiscover = (Button) findViewById(R.id.btDiscover);
        btPositive = (Button) findViewById(R.id.btLike);
        btNegative = (Button) findViewById(R.id.btDislike);

        if (btHungry != null && btDiscover != null && btPositive != null && btNegative != null) {
            btHungry.setOnTouchListener(this);
            btHungry.setOnClickListener(this);
            btDiscover.setOnTouchListener(this);
            btDiscover.setOnClickListener(this);
            btPositive.setOnClickListener(this);
            btPositive.setOnTouchListener(this);
            btNegative.setOnClickListener(this);
            btNegative.setOnTouchListener(this);

            // Note: Since Lollipop buttons always appear in front because of a StateListAnimator.
            // To resolve this we check version Lollipop and above to deactivate this StateListAnimator.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btPositive.setStateListAnimator(null);
                btNegative.setStateListAnimator(null);
            }
        }
    }

    public void collapseToolbar() {
        final AppBarLayout appBar = getAppBarLayout();

        // Measurement
        TypedValue tv = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);

        final int initialHeight = appBar.getHeight();
        final int toolbarHeight = getResources().getDimensionPixelSize(tv.resourceId);
        final int finalHeight = initialHeight - toolbarHeight;

        // Make toolbar collapsible
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) getCollapsingToolbar().getLayoutParams();
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
        getCollapsingToolbar().setLayoutParams(params);

        // Fling toolbar after a few milliseconds
        final CoordinatorLayout.LayoutParams appBarParams = (CoordinatorLayout.LayoutParams) appBar.getLayoutParams();
        final AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) appBarParams.getBehavior();
        new android.os.Handler().postDelayed(new Runnable() {
            public void run() {
                behavior.onNestedFling((CoordinatorLayout) findViewById(R.id.coordinator_layout), appBar, null, 0, 9000, false);
            }
        }, 1);

        // Listen for toolbar fling
        appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                // Disable collapsing toolbar
                if (Math.abs(verticalOffset) == Math.abs(finalHeight)) {
                    appBarParams.height = toolbarHeight;
                    appBarLayout.requestLayout();
                    appBarLayout.findViewById(R.id.toolbar_layout).animate().alpha(1).setDuration(400);

                    isCollapsed = true;

                    if (isLocated) {
                        firstStart();
                    }

                }

                // Fade out intro content
                if (verticalOffset == 0) {
                    appBarLayout.findViewById(R.id.introImage).animate().alpha(0).setDuration(400);
                    appBarLayout.findViewById(R.id.introLayout).animate().alpha(0).setDuration(400);
                }
            }
        });
    }

    private void displayUI() {
        Button btLike = (Button) findViewById(R.id.btLike);
        Button btDislike = (Button) findViewById(R.id.btDislike);
        btLike.setVisibility(View.VISIBLE);
        btLike.animate().alpha(1).setDuration(200);
        btDislike.setVisibility(View.VISIBLE);
        btDislike.animate().alpha(1).setDuration(200);
    }

    private void setIntentFilter() {
        IntentFilter dataFilter = new IntentFilter(Constants.BROADCAST_ACTION);
        CardsReceiver dataReceiver = new CardsReceiver(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(dataReceiver, dataFilter);

        IntentFilter tokenFilter = new IntentFilter(Constants.BROADCAST_TOKEN);
        TokenReceiver tokenReceiver = new TokenReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(tokenReceiver, tokenFilter);

        IntentFilter statusFilter = new IntentFilter(Constants.BROADCAST_STATUS);
        StatusReceiver statusReceiver = new StatusReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(statusReceiver, statusFilter);
    }

    /* On touch and on click listener */

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btHungry:
                eatMode = true;
                modeSwitch.setChecked(true);
            case R.id.btDiscover:
                collapseToolbar();
                break;
            case R.id.btLike:
                swipeCards.selectRight();
                break;
            case R.id.btDislike:
                swipeCards.selectLeft();
                break;
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (v.getId() == R.id.switch_compat) {
            switchTouched = true;
            if (!isCollapsed) collapseToolbar();
        } else {
            int action = MotionEventCompat.getActionMasked(event);

            if (action == MotionEvent.ACTION_DOWN) {
                // Feedback on touch down
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
        }

        return false;
    }

    /* Radius Dialog Listener */

    @Override
    public void onRadiusDialogPositiveClick(RadiusDialogFragment dialog) {
        radius = dialog.getRadius();
        getDrawerLayout().closeDrawers();

        if (isLocated) {
            swipeCards.resetCards();
            database.truncateDishes();
            startDataService();
        }
    }

    @Override
    public void onNoResultDialogPositiveClick(NoResultDialogFragment dialog) {
        RadiusDialogFragment radiusDialog = new RadiusDialogFragment();
        radiusDialog.setRadius(radius);
        radiusDialog.show(getFragmentManager(), RadiusDialogFragment.class.getSimpleName());
    }
}
