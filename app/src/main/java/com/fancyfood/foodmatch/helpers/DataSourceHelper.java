package com.fancyfood.foodmatch.helpers;

import android.content.Context;

import com.fancyfood.foodmatch.data.DishesDataSource;
import com.fancyfood.foodmatch.data.RatingsDataSource;
import com.fancyfood.foodmatch.models.Card;
import com.fancyfood.foodmatch.models.Rating;

import java.util.ArrayList;

public class DataSourceHelper {

    private DishesDataSource dishesDataSource;
    private RatingsDataSource ratingsDataSource;

    public DataSourceHelper(Context context) {
        dishesDataSource = new DishesDataSource(context);
        ratingsDataSource = new RatingsDataSource(context);
    }

    /* Ratings Table */

    public void addPositiveRating(Card card) {
        Rating rating = new Rating(card.getDishId(), true, card.getLocation(), null);
        ratingsDataSource.createRating(rating);
    }

    public void addNegativeRating(Card card) {
        Rating rating = new Rating(card.getDishId(), false, card.getLocation(), null);
        ratingsDataSource.createRating(rating);
    }

    /* Dishes Table */

    public void addCardsBatch(ArrayList<Card> cardsList) {
        // Loop through all cards
        for (Card card : cardsList) addCard(card);
    }

    public void addCard(Card card) {
        dishesDataSource.insertDish(card);
    }

    public void truncateDishes() {
        dishesDataSource.truncate();
    }

    public ArrayList<Card> getCurrentCards(int limit) {
        return dishesDataSource.getAllDishes(limit);
    }

    public Card getCard() {
        return dishesDataSource.getFirstData();
    }



//    private void insertRating(Card card, boolean rating) {
//        //--------------------------------------------------------------------------
//        //FOR DATABASE
//        //Cast dataObject to Card to use get and set methods
//        Rating newRating = new Rating(card.getDishId(), rating, card.getLocation(), null);
//
//        //write data to database
//        Rating ratingMemo = dataSource.createRating(newRating);
//
//        //only for testing purposes
//        Log.d(TAG, "Es wurde der folgende Eintrag in die Datenbank geschrieben:");
//        Log.d(TAG, "Gericht: " + ratingMemo.getDishId());
//        //testing getting all elements from database
//        List<Rating> InhaltDB = dataSource.getAllRatingMemos();
//        Log.d(TAG, "number of element in the DB: " + InhaltDB.size());
//        //--------------------------------------------------------------------------
//    }

}
