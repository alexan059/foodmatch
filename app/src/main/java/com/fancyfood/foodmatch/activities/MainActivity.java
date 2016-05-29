package com.fancyfood.foodmatch.activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.fancyfood.foodmatch.R;
import com.fancyfood.foodmatch.adapters.CardAdapter;
import com.fancyfood.foodmatch.models.Card;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    // Rating cards
    private CardAdapter cardAdapter;
    private ArrayList<Card> al;
    // For rating system
    private int like, dislike;
    private TextView tLikes, tDislikes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateView(R.layout.activity_main); // Method declared in BaseActivity class to replace setContentView()

        // Initialize rating system
        like = dislike = 0;
        tLikes = (TextView) findViewById(R.id.textLikes);
        tDislikes = (TextView) findViewById(R.id.textDislikes);

        // Find the Fling Container
        SwipeFlingAdapterView flingContainer = (SwipeFlingAdapterView) findViewById(R.id.rating_cards);

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
                    dislike++;
                    tDislikes.setText("Dislikes: " + Integer.toString(dislike));
                }

                @Override
                public void onRightCardExit(Object dataObject) {
                    like++;
                    tLikes.setText("Likes: " + Integer.toString(like));
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
        }
        //shuffle List for randomize picture order
        Collections.shuffle(al);
    }
}
