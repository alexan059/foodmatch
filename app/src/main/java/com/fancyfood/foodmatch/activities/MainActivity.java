package com.fancyfood.foodmatch.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
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

import com.fancyfood.foodmatch.R;
import com.fancyfood.foodmatch.adapters.CardAdapter;
import com.fancyfood.foodmatch.data.RatingDataSource;
import com.fancyfood.foodmatch.fragments.RadiusDialogFragment;
import com.fancyfood.foodmatch.models.Card;
import com.fancyfood.foodmatch.models.Rating;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.view.View.OnClickListener;
import static android.view.View.OnTouchListener;

public class MainActivity extends BaseActivity implements OnClickListener, OnTouchListener,
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener, OnCheckedChangeListener,
        OnNavigationItemSelectedListener, RadiusDialogFragment.RadiusDialogListener {

    // Debug Tag
    private static final String TAG = MainActivity.class.getSimpleName();

    // App Constants
    private static final int MY_PERMISSIONS_COARSE_LOCATIONS = 64;

    // Shared Preferences
    public static final String FM_PREFS = "FoodmatchPreferences";

    // Defaults
    public static final int DEF_RADIUS = 5;

    // Views
    private SwitchCompat modeSwitch;
    private ProgressBar progressBar;

    // Rating cards
    private CardAdapter cardAdapter;
    private ArrayList<Card> al;
    private SwipeFlingAdapterView flingContainer;
    private RatingDataSource dataSource;

    // Location client
    private GoogleApiClient googleApiClient;
    private static Location currentLocation;
    private LocationRequest locationRequest;
    private int radius;

    // Flags
    private boolean flingStarted = false;
    private boolean eatMode = false;
    private boolean switchTouched = false;

    /* Activity lifecycle */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateView(R.layout.activity_main);

        initButtons();

        // Create instance of GoogleAPIClient
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient
                    .Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // Get Switch Compat
        modeSwitch = (SwitchCompat) getMenu().findItem(R.id.nav_switch)
                .getActionView().findViewById(R.id.switch_compat);
        modeSwitch.setOnTouchListener(this);
        modeSwitch.setOnCheckedChangeListener(this);

        // Set navigation
        getNavigation().setNavigationItemSelectedListener(this);

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(FM_PREFS, 0);
        radius = settings.getInt("foodRadius", DEF_RADIUS);

        // Progress bar
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        // Init Database
        dataSource = new RatingDataSource(this);
        Log.d(TAG, "Die Datenquelle wird geöffnet.");
        dataSource.open();
    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        stopLocationUpdates();
        googleApiClient.disconnect();
        super.onStop();

        // Store changed preferences
        SharedPreferences settings = getSharedPreferences(FM_PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("foodRadius", radius);
        editor.apply();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (googleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    /* Async Task */

    public class LoadCardsTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            while (currentLocation == null) {
                // Nop
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            initFling();
            displayUI();
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    /* Helper methods */

    private void initTasks() {
        LoadCardsTask task = new LoadCardsTask();
        task.execute();
    }

    public void initButtons() {
        final Button btHungry = (Button) findViewById(R.id.btHungry);
        final Button btDiscover = (Button) findViewById(R.id.btDiscover);
        final Button btLike = (Button) findViewById(R.id.btLike);
        final Button btDislike = (Button) findViewById(R.id.btDislike);

        if (btHungry != null && btDiscover != null && btLike != null && btDislike != null) {
            btHungry.setOnTouchListener(this);
            btHungry.setOnClickListener(this);
            btDiscover.setOnTouchListener(this);
            btDiscover.setOnClickListener(this);
            btLike.setOnClickListener(this);
            btLike.setOnTouchListener(this);
            btDislike.setOnClickListener(this);
            btDislike.setOnTouchListener(this);

            // Note: Since Lollipop buttons always appear in front because of a StateListAnimator.
            // To resolve this we check version Lollipop and above to deactivate this StateListAnimator.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btLike.setStateListAnimator(null);
                btDislike.setStateListAnimator(null);
            }
        }
    }

    final public void initFling() {
        // Stop if already started or location isn't ready
        if (flingStarted || currentLocation == null)
            return;

        // Find the Fling Container
        flingContainer = (SwipeFlingAdapterView) findViewById(R.id.rating_cards);

        // Initialize Card List
        al = new ArrayList<Card>();
        // Add dummy data to Card List
        addDummy();
        // Connect layout to Card List with Card Adapter
        cardAdapter = new CardAdapter(this, R.layout.card_item, al);

        if (flingContainer != null) {
            // Pass Card Adapter to Fling Container
            flingContainer.setAdapter(cardAdapter);

            // Implement Interface Methods to handle actions
            flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
                @Override
                public void removeFirstObjectInAdapter() {
                    al.remove(0); // Remove first object in adapter
                    cardAdapter.notifyDataSetChanged();
                }

                @Override
                public void onLeftCardExit(Object dataObject) {
                    // Item disliked
                    Card currentCard = (Card) dataObject;
                    insertRating(currentCard, false);
                }

                @Override
                public void onRightCardExit(Object dataObject) {
                    // Item liked
                    Card currentCard = (Card) dataObject;
                    insertRating(currentCard, true);

                    if (eatMode) {
                        // Method to change Activity ->get MapsActivity
                        Intent i = new Intent(MainActivity.this, MapsActivity.class);

                        double lat = currentCard.getPosition().getLatitude();
                        double lng = currentCard.getPosition().getLongitude();

                        // Add data to Intent to use them in MapActivity
                        i.putExtra("Lat", lat);
                        i.putExtra("Lng", lng);

                        // StartMapsActivity
                        startActivity(i);
                    }
                }

                @Override
                public void onAdapterAboutToEmpty(int itemsInAdapter) {
                    // Ask for more data here
                    addDummy();
                    cardAdapter.notifyDataSetChanged();
                }

                @Override
                public void onScroll(float v) {
                }
            });

            // Optionally add an OnItemClickListener
            flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
                @Override
                public void onItemClicked(int itemPosition, Object dataObject) {
                }
            });

            flingStarted = true;

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
                if (verticalOffset == -(finalHeight)) {
                    appBarParams.height = toolbarHeight;
                    appBarLayout.requestLayout();
                    appBarLayout.findViewById(R.id.toolbar_layout).animate().alpha(1).setDuration(400);

                    initTasks();
                }

                // Fade out intro content
                if (verticalOffset == 0) {
                    appBarLayout.findViewById(R.id.introImage).animate().alpha(0).setDuration(400);
                    appBarLayout.findViewById(R.id.introLayout).animate().alpha(0).setDuration(400);
                }
            }
        });
    }

    private void insertRating(Card card, boolean rating) {
        //--------------------------------------------------------------------------
        //FOR DATABASE
        //Cast dataObject to Card to use get and set methods
        Rating newRating = new Rating(card.getID(), rating, card.getPosition(), null);

        //write data to database
        Rating ratingMemo = dataSource.createRating(newRating);

        //only for testing purposes
        Log.d(TAG, "Es wurde der folgende Eintrag in die Datenbank geschrieben:");
        Log.d(TAG, "Gericht: " + ratingMemo.getID());
        //testing getting all elements from database
        List<Rating> InhaltDB = dataSource.getAllRatingMemos();
        Log.d(TAG, "number of element in the DB: " + InhaltDB.size());
        //--------------------------------------------------------------------------
    }

    /* View Helper */

    private void displayUI() {
        flingContainer.setVisibility(View.VISIBLE);
        flingContainer.animate().alpha(1).setDuration(200);

        Button btLike = (Button) findViewById(R.id.btLike);
        Button btDislike = (Button) findViewById(R.id.btDislike);
        btLike.setVisibility(View.VISIBLE);
        btLike.animate().alpha(1).setDuration(200);
        btDislike.setVisibility(View.VISIBLE);
        btDislike.animate().alpha(1).setDuration(200);

        flingContainer.requestLayout();
    }

    /* Dummy data */

    /**
     * Short version to get drawable from resource id.
     *
     * @param imageId
     * @return
     */
    public Drawable getSupportDrawable(int imageId) {
        return ContextCompat.getDrawable(getApplicationContext(), imageId);
    }

    /**
     * Add some dummy data to Card List.
     */
    public void addDummy() {
        if (al != null) {
            al.add(new Card(md5("1"), currentLocation, getSupportDrawable(R.drawable.currywurst), "Currywurst", "Curry 36 - Kreuzberg", 1050.5, 1));
            al.add(new Card(md5("2"), currentLocation, getSupportDrawable(R.drawable.fishnchips), "Fish'n'Chips", "Nordsee - Alexa", 440, 1));
            al.add(new Card(md5("3"), currentLocation, getSupportDrawable(R.drawable.hamburger), "Hamburger", "Kreuzburger - Prenzlauer Berg", 600.8, 1));
            al.add(new Card(md5("4"), currentLocation, getSupportDrawable(R.drawable.lasagna), "Lasagne", "Picasso - Mitte", 300, 2));
            al.add(new Card(md5("5"), currentLocation, getSupportDrawable(R.drawable.pizza), "Pizza", "Livoro - Prenzlauer Berg", 400, 2));
            al.add(new Card(md5("6"), currentLocation, getSupportDrawable(R.drawable.springrolls), "Frühligsrollen", "Vietnam Village - Mitte", 450, 1));
            al.add(new Card(md5("7"), currentLocation, getSupportDrawable(R.drawable.steak), "Steak", "Blockhouse - Zoo", 2005, 2));
            al.add(new Card(md5("8"), currentLocation, getSupportDrawable(R.drawable.sushi), "Sushi", "Sushi Circle - Zehelndorf", 4000, 3));

            //shuffle List for randomize picture order
            Collections.shuffle(al);
        }
    }

    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
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
                flingContainer.getTopCardListener().selectRight();
                break;
            case R.id.btDislike:
                flingContainer.getTopCardListener().selectLeft();
                break;
            default:
                // Do nothing
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (v.getId() == R.id.switch_compat) {
            switchTouched = true;
            if (!flingStarted)
                collapseToolbar();
        } else {
            int action = MotionEventCompat.getActionMasked(event);

            if (action == MotionEvent.ACTION_DOWN) {
                // Feedback on touch down
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
        }

        return false;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (switchTouched) {
            switchTouched = false;
            eatMode = isChecked;

            Toast.makeText(this, "\"Sofort essen\" wurde " + (!isChecked ? "de":"") + "aktiviert.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.nav_radius:
                RadiusDialogFragment dialog = new RadiusDialogFragment();
                dialog.setRadius(radius);
                dialog.show(getFragmentManager(), RadiusDialogFragment.class.getSimpleName());
                break;
            case R.id.nav_settings:
                break;
        }

        return false;
    }

    /* Radius Dialog Listener */

    @Override
    public void onDialogPositiveClick(RadiusDialogFragment dialog) {
        radius = dialog.getRadius();
        Log.d(TAG, "Radius set: " + Integer.toString(radius) + "00 m");
    }

    /* Location Helper */

    protected void createLocationRequests() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        // API 23+ Check explicitly for granted permissions or retrieve them
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
           ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, MY_PERMISSIONS_COARSE_LOCATIONS);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    /* Google Api Client */

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // API 23+ Check explicitly for granted permissions or retrieve them
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, MY_PERMISSIONS_COARSE_LOCATIONS);
        }

        // Get last location and store it
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        // Show values of last location
        if (currentLocation != null) {
            // Update something
        }

        createLocationRequests();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        Log.d(TAG, "lat: " + Double.toString(currentLocation.getLatitude()) + " lng: " + Double.toString(currentLocation.getLongitude()));
    }
}
