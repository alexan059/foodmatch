package com.fancyfood.foodmatch.modules;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.fancyfood.foodmatch.R;
import com.fancyfood.foodmatch.activities.MainActivity;
import com.fancyfood.foodmatch.activities.MapsActivity;
import com.fancyfood.foodmatch.adapters.CardAdapter;
import com.fancyfood.foodmatch.helpers.DataSourceHelper;
import com.fancyfood.foodmatch.models.Card;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;
import java.util.Collections;

public class SwipeCards implements SwipeFlingAdapterView.onFlingListener, SwipeFlingAdapterView.OnItemClickListener{

    private Context context;
    private DataSourceHelper database;
    private SwipeFlingAdapterView container;

    private CardAdapter adapter;
    private ArrayList<Card> cards;

    private OnFlingCallbackListener listener;

    public interface OnFlingCallbackListener {
        void startMapsActivity(Card card);
    }

    public SwipeCards(Context context, SwipeFlingAdapterView container) {
        this.context = context;
        this.container = container;

        database = new DataSourceHelper(context);
        intitialize();
    }

    private void intitialize() {
        cards = new ArrayList<>();
        adapter = new CardAdapter(context, R.layout.card_item, cards);
        container.setAdapter(adapter);
        container.setOnItemClickListener(this);
        container.setFlingListener(this);
    }

    public void setOnFlingCallbackListener(OnFlingCallbackListener listener) {
        this.listener = listener;
    }

    public void display() {
        container.setVisibility(View.VISIBLE);
        container.animate().alpha(1).setDuration(200);
        container.requestLayout();
    }

    public void selectRight() {
        container.getTopCardListener().selectRight();
    }

    public void selectLeft() {
        container.getTopCardListener().selectLeft();
    }

    public void appendCards(ArrayList<Card> cardsList) {
        Collections.shuffle(cardsList);
        cards.addAll(cardsList);
    }

    @Override
    public void onItemClicked(int index, Object object) {

    }

    @Override
    public void removeFirstObjectInAdapter() {
        cards.remove(0); // Remove first object in adapter
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLeftCardExit(Object object) {
        database.addNegativeRating((Card)object);
    }

    @Override
    public void onRightCardExit(Object object) {
        database.addPositiveRating((Card)object);
        listener.startMapsActivity((Card)object);
    }

    @Override
    public void onAdapterAboutToEmpty(int items) {
        //if (itemsInAdapter < 1)
        //    startDataService();
    }

    @Override
    public void onScroll(float v) {

    }
}
