package com.fancyfood.foodmatch.activities;

import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.fancyfood.foodmatch.R;
import com.fancyfood.foodmatch.adapters.CardAdapter;
import com.fancyfood.foodmatch.models.Card;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;
import java.util.Collections;

import static android.view.View.OnClickListener;
import static android.view.View.OnTouchListener;

public class MainActivity extends BaseActivity implements OnClickListener, OnTouchListener
{

    private static final String TAG = MainActivity.class.getSimpleName();

    // Rating cards
    private CardAdapter cardAdapter;
    private String dish;
    private ArrayList<Card> al;
    private SwipeFlingAdapterView flingContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateView(R.layout.activity_main);

        Button btHungry = (Button) findViewById(R.id.btHungry);
        Button btDiscover = (Button) findViewById(R.id.btDiscover);
        Button btLike = (Button) findViewById(R.id.btLike);
        Button btDislike = (Button) findViewById(R.id.btDislike);

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

        initFling();

    }

    final public void initFling() {
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
                    al.remove(0); // Remove first
                    cardAdapter.notifyDataSetChanged();
                }

                @Override
                public void onLeftCardExit(Object dataObject) {
                    Toast.makeText(getApplicationContext(), "DISLIKE", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRightCardExit(Object dataObject) {
                    Toast.makeText(getApplicationContext(), "LIKE", Toast.LENGTH_SHORT).show();
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
        }
    }

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
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL|AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
        getCollapsingToolbar().setLayoutParams(params);

        // Fling toolbar after a few milliseconds
        final CoordinatorLayout.LayoutParams appBarParams = (CoordinatorLayout.LayoutParams) appBar.getLayoutParams();
        final AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) appBarParams.getBehavior();
        new android.os.Handler().postDelayed(new Runnable() {public void run() {
            behavior.onNestedFling((CoordinatorLayout) findViewById(R.id.coordinator_layout), appBar, null, 0, 9000, false);
        }}, 1);

        // Listen for toolbar fling
        appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                // Disable collapsing toolbar
                if (verticalOffset == -(finalHeight)) {
                    appBarParams.height = toolbarHeight;
                    appBarLayout.requestLayout();
                    appBarLayout.findViewById(R.id.toolbar_layout).animate().alpha(1).setDuration(400);
                }

                // Fade out intro content
                if (verticalOffset == 0) {
                    appBarLayout.findViewById(R.id.introImage).animate().alpha(0).setDuration(400);
                    appBarLayout.findViewById(R.id.introLayout).animate().alpha(0).setDuration(400);
                }
            }
        });
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
}
