package com.fancyfood.foodmatch.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.fancyfood.foodmatch.R;
import com.fancyfood.foodmatch.adapters.CardAdapter;
import com.fancyfood.foodmatch.models.Card;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;
import java.util.Collections;

import static android.view.View.OnClickListener;
import static android.view.View.OnTouchListener;

public class MainActivity extends BaseActivity implements OnClickListener, OnTouchListener,
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_COARSE_LOCATIONS = 64;

    // Rating cards
    private CardAdapter cardAdapter;
    private ArrayList<Card> al;
    private SwipeFlingAdapterView flingContainer;
    private LikeDataSource dataSource;

    // Location client
    private GoogleApiClient googleApiClient;
    private static Location currentLocation;
    private LocationRequest locationRequest;

    // Check flag for intent
    private boolean flingStarted = false;

    /* Activity lifecycle */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateView(R.layout.activity_main);

        Button btHungry = (Button) findViewById(R.id.btHungry);
        Button btDiscover = (Button) findViewById(R.id.btDiscover);
        Button btLike = (Button) findViewById(R.id.btLike);
        Button btDislike = (Button) findViewById(R.id.btDislike);
        dataSource = new LikeDataSource(this);                                                      //object for database management
        Log.d(LOG_TAG, "Die Datenquelle wird geöffnet.");                                           //open  database
        dataSource.open();


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

        // Create instance of GoogleAPIClient
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient
                    .Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
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
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (googleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    /* Helper methods */

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
                }

                @Override
                public void onRightCardExit(Object dataObject) {
                    // Item liked
                    Toast.makeText(getApplicationContext(), "LIKE", Toast.LENGTH_SHORT).show();

                    //--------------------------------------------------------------------------------------------------------------------------------------------------
                    //FOR DATABASE
                    Card currentCard=(Card) dataObject;                                             //Cast dataObject to Card to use get and set methods
                    String dish = currentCard.getDish();
                    String location = currentCard.getLocation();

                    dataSource.createCard(dish,location);                                           //write data to database
                    //Log.d(LOG_TAG, "Die Datenquelle wird geschlossen.");
                    //dataSource.close();
                    //---------------------------------------------------------------------------------------------------------------------------------------------------

                    // Method to change Activity ->get MapsActivity
                    Intent i = new Intent(MainActivity.this, MapsActivity.class);
                    // Add data to Intent to use them in MapActivity
                    i.putExtra("Lat", currentLocation.getLatitude());
                    i.putExtra("Lng", currentLocation.getLongitude());

                    // StartMapsActivity
                    startActivity(i);

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

                    initFling();
                }

                // Fade out intro content
                if (verticalOffset == 0) {
                    appBarLayout.findViewById(R.id.introImage).animate().alpha(0).setDuration(400);
                    appBarLayout.findViewById(R.id.introLayout).animate().alpha(0).setDuration(400);
                }
            }
        });
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
            al.add(new Card(getSupportDrawable(R.drawable.currywurst), "Currywurst", "Curry 36 - Kreuzberg", 1050.5, 1));
            al.add(new Card(getSupportDrawable(R.drawable.fishnchips), "Fish'n'Chips", "Nordsee - Alexa", 440, 1));
            al.add(new Card(getSupportDrawable(R.drawable.hamburger), "Hamburger", "Kreuzburger - Prenzlauer Berg", 600.8, 1));
            al.add(new Card(getSupportDrawable(R.drawable.lasagna), "Lasagne", "Picasso - Mitte", 300, 2));
            al.add(new Card(getSupportDrawable(R.drawable.pizza), "Pizza", "Livoro - Prenzlauer Berg", 400, 2));
            al.add(new Card(getSupportDrawable(R.drawable.springrolls), "Frühligsrollen", "Vietnam Village - Mitte", 450, 1));
            al.add(new Card(getSupportDrawable(R.drawable.steak), "Steak", "Blockhouse - Zoo", 2005, 2));
            al.add(new Card(getSupportDrawable(R.drawable.sushi), "Sushi", "Sushi Circle - Zehelndorf", 4000, 3));

            //shuffle List for randomize picture order
            Collections.shuffle(al);

        }
    }

    /* On touch and on click listener */

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btHungry:
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
        int action = MotionEventCompat.getActionMasked(event);

        if (action == MotionEvent.ACTION_DOWN) {
            // Feedback on touch down
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        }

        return false;
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
            initFling();
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
        initFling();
    }

    @Override
    protected void onPause() {                                                                      //close Database when MainActivity pause
        super.onPause();

        Log.d(LOG_TAG, "Die Datenquelle wird geschlossen.");
        dataSource.close();
    }

}
