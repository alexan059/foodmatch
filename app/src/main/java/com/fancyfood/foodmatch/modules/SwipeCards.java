package com.fancyfood.foodmatch.modules;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.fancyfood.foodmatch.R;
import com.fancyfood.foodmatch.adapters.CardAdapter;
import com.fancyfood.foodmatch.helpers.DataSourceHelper;
import com.fancyfood.foodmatch.models.Card;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SwipeCards implements SwipeFlingAdapterView.onFlingListener, SwipeFlingAdapterView.OnItemClickListener{

    private static final String TAG = SwipeCards.class.getSimpleName();

    private Context context;
    private DataSourceHelper database;
    private SwipeFlingAdapterView container;

    private CardAdapter adapter;
    private ArrayList<Card> cards;

    private OnFlingCallbackListener listener;

    public interface OnFlingCallbackListener {
        void startMapsActivity(Card card);
        void requestCards();
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
        if (cards.size() < 1) cards.addAll(cardsList);
        adapter.notifyDataSetChanged();
    }

    public void resetCards() {
        cards.clear();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClicked(int index, Object object) {

    }

    @Override
    public void removeFirstObjectInAdapter() {
        database.consumeDish(cards.get(0));
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
        if (items == 1)
            listener.requestCards();
    }

    @Override
    public void onScroll(float v) {

    }
}
