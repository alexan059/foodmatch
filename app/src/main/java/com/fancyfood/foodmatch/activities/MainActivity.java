package com.fancyfood.foodmatch.activities;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
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

import com.fancyfood.foodmatch.data.DishesDataSource;
import com.fancyfood.foodmatch.preferences.Constants;
import com.fancyfood.foodmatch.core.CoreActivity;
import com.fancyfood.foodmatch.core.CoreApplication;
import com.fancyfood.foodmatch.R;
import com.fancyfood.foodmatch.adapters.CardAdapter;
import com.fancyfood.foodmatch.data.RatingsDataSource;
import com.fancyfood.foodmatch.fragments.RadiusDialogFragment;
import com.fancyfood.foodmatch.fragments.RadiusDialogFragment.RadiusDialogListener;
import com.fancyfood.foodmatch.helpers.GoogleApiLocationHelper;
import com.fancyfood.foodmatch.helpers.GoogleApiLocationHelper.OnLocationChangedListener;
import com.fancyfood.foodmatch.models.Card;
import com.fancyfood.foodmatch.models.Rating;
import com.fancyfood.foodmatch.services.CardReciever;
import com.fancyfood.foodmatch.services.CardsPullService;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.view.View.OnClickListener;
import static android.view.View.OnTouchListener;

public class MainActivity extends CoreActivity implements OnClickListener, OnTouchListener,
        OnCheckedChangeListener, OnLocationChangedListener, OnNavigationItemSelectedListener, RadiusDialogListener,
        CardReciever.OnDataReceiveListener {

    // Debug Tag
    private static final String TAG = MainActivity.class.getSimpleName();

    // Shared Preferences
    public static final String FOODMATCH_PREFERENCES = "com.fancyfood.foodmatch.preferences";

    // Defaults
    public static final int DEFAULT_RADIUS = 5;

    // Views
    private SwitchCompat modeSwitch;
    private ProgressBar progressBar;

    // Rating cards
    private CardAdapter cardAdapter;
    private ArrayList<Card> al;
    private SwipeFlingAdapterView flingContainer;
    private RatingsDataSource dataSource;
    private DishesDataSource dishesDataSource;

    // Location and radius settings
    private GoogleApiLocationHelper locationHelper;
    private Location currentLocation;
    private int radius;

    // Flags
    private boolean flingStarted = false;
    private boolean eatMode = false;
    private boolean switchTouched = false;

    /* Activity Lifecycle */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateView(R.layout.activity_main);

        initButtons();

        // Get Switch Compat
        modeSwitch = (SwitchCompat) getMenu().findItem(R.id.nav_switch)
                .getActionView().findViewById(R.id.switch_compat);
        modeSwitch.setOnTouchListener(this);
        modeSwitch.setOnCheckedChangeListener(this);

        // Set navigation
        getNavigation().setNavigationItemSelectedListener(this);

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(FOODMATCH_PREFERENCES, 0);
        radius = settings.getInt("foodRadius", DEFAULT_RADIUS);

        // Progress bar
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        // Init Database
        dataSource = new RatingsDataSource(this);
        Log.d(TAG, "Die Datenquelle wird geöffnet.");
        dataSource.open();

        dishesDataSource = new DishesDataSource(this);

        if (CoreApplication.getGoogleApiHelper() != null) {
            locationHelper = CoreApplication.getGoogleApiHelper();
            locationHelper.setOnLocationChangedListener(this);
        }

        IntentFilter filter = new IntentFilter(Constants.BROADCAST_ACTION);
        CardReciever reciever = new CardReciever(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(reciever, filter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (locationHelper != null && !locationHelper.isConnected()) {
            locationHelper.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (locationHelper != null && locationHelper.isConnected()) {
            locationHelper.disconnect();
        }

        // Store changed preferences
        SharedPreferences settings = getSharedPreferences(FOODMATCH_PREFERENCES, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("foodRadius", radius);
        editor.apply();
    }

    /* IntentService for getting data */

    private void startDataService() {
        // "restaurants/55.56/57.6/2000" -> resource/lat/lng/radius
        // http://api.collective-art.de/restaurants/13.5264438/52.4569312/2000?pretty&token=n5DUfSC72hPABeEhu89Ex63soJ2oJCQfTxlim8MC6oHVLlrutMa3xDjDursL
        String lat = "13.5264438"; //Double.toString(currentLocation.getLatitude());
        String lng = "52.4569312"; //Double.toString(currentLocation.getLongitude());
        String rad = "2000"; //Integer.toString(radius) + "00";

        String uri = "restaurants/" + lat + "/" + lng + "/" + rad;

        Intent intent = new Intent(this, CardsPullService.class);
        intent.setData(Uri.parse(uri));
        startService(intent);
    }

    @Override
    public void onDataReceive() {
        Card card = dishesDataSource.getFirstData();
        al.add(card);
        cardAdapter.notifyDataSetChanged();
    }

    /* Google Api Helper and Location Listener */

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
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
        al = new ArrayList<>();
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

                        double lat = currentCard.getLocation().getLatitude();
                        double lng = currentCard.getLocation().getLongitude();

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
                    if (itemsInAdapter < 1)
                        startDataService();
                }

                @Override
                public void onScroll(float v) {

                }
            });

            flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
                @Override
                public void onItemClicked(int itemPosition, Object dataObject) {

                }
            });

            flingStarted = true;

            startDataService();

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
        Rating newRating = new Rating(card.getReference(), rating, card.getLocation(), null);

        //write data to database
        Rating ratingMemo = dataSource.createRating(newRating);

        //only for testing purposes
        Log.d(TAG, "Es wurde der folgende Eintrag in die Datenbank geschrieben:");
        Log.d(TAG, "Gericht: " + ratingMemo.getReference());
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

//    /* Dummy data */
//
//    /**
//     * Short version to get drawable from resource id.
//     *
//     * @param imageId
//     * @return
//     */
//    public Drawable getSupportDrawable(int imageId) {
//        return ContextCompat.getDrawable(getApplicationContext(), imageId);
//    }
//
//    /**
//     * Add some dummy data to Card List.
//     */
//    public void addDummy() {
//        if (al != null) {
//            al.add(new Card(md5("1"), currentLocation, getSupportDrawable(R.drawable.currywurst), "Currywurst", "Curry 36 - Kreuzberg", 1050.5, 1));
//            al.add(new Card(md5("2"), currentLocation, getSupportDrawable(R.drawable.fishnchips), "Fish'n'Chips", "Nordsee - Alexa", 440, 1));
//            al.add(new Card(md5("3"), currentLocation, getSupportDrawable(R.drawable.hamburger), "Hamburger", "Kreuzburger - Prenzlauer Berg", 600.8, 1));
//            al.add(new Card(md5("4"), currentLocation, getSupportDrawable(R.drawable.lasagna), "Lasagne", "Picasso - Mitte", 300, 2));
//            al.add(new Card(md5("5"), currentLocation, getSupportDrawable(R.drawable.pizza), "Pizza", "Livoro - Prenzlauer Berg", 400, 2));
//            al.add(new Card(md5("6"), currentLocation, getSupportDrawable(R.drawable.springrolls), "Frühligsrollen", "Vietnam Village - Mitte", 450, 1));
//            al.add(new Card(md5("7"), currentLocation, getSupportDrawable(R.drawable.steak), "Steak", "Blockhouse - Zoo", 2005, 2));
//            al.add(new Card(md5("8"), currentLocation, getSupportDrawable(R.drawable.sushi), "Sushi", "Sushi Circle - Zehelndorf", 4000, 3));
//
//            //shuffle List for randomize picture order
//            Collections.shuffle(al);
//        }
//    }
//
//    public static String md5(final String s) {
//        final String MD5 = "MD5";
//        try {
//            // Create MD5 Hash
//            MessageDigest digest = java.security.MessageDigest
//                    .getInstance(MD5);
//            digest.update(s.getBytes());
//            byte messageDigest[] = digest.digest();
//
//            // Create Hex String
//            StringBuilder hexString = new StringBuilder();
//            for (byte aMessageDigest : messageDigest) {
//                String h = Integer.toHexString(0xFF & aMessageDigest);
//                while (h.length() < 2)
//                    h = "0" + h;
//                hexString.append(h);
//            }
//            return hexString.toString();
//
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//        return "";
//    }

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
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (v.getId() == R.id.switch_compat) {
            switchTouched = true;
            if (!flingStarted) collapseToolbar();
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

}
