package com.fancyfood.foodmatch.util;

import android.location.Location;
import android.util.Log;

import com.fancyfood.foodmatch.helpers.HttpConnectionHelper;
import com.fancyfood.foodmatch.models.Card;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;

public final class JSONCardsParser {

    private static final String TAG = JSONCardsParser.class.getSimpleName();

    public static ArrayList<Card> parse(JSONArray array) {
        try {
            ArrayList<Card> cardsList = new ArrayList<>();

            // Loop through all elements
            for (int i = 0; i < array.length(); i++) {
                cardsList.addAll(getDishesFromRestaurant(array.getJSONObject(i)));
            }

            return cardsList;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ArrayList<Card> getDishesFromRestaurant(JSONObject restaurant) throws JSONException {
        JSONArray dishes = restaurant.getJSONArray("dishes");

        ArrayList<Card> cardsList = new ArrayList<>();

        Log.d(TAG, restaurant.getString("name") + " has " + Integer.toString(dishes.length()) + " dishes.");

        // Loop through all elements
        for (int i = 0; i < dishes.length(); i++) {
            cardsList.add(createCard(dishes.getJSONObject(i), restaurant));
        }

        return cardsList;
    }

    public static Card createCard(JSONObject dish, JSONObject restaurant) throws JSONException {
        // Create empty card
        Card card = new Card();

        // Set common information
        card.setDishId(dish.getString("_id"));
        card.setDish(dish.getString("name"));
        card.setLocationId(restaurant.getString("_id"));
        card.setLocationName(restaurant.getString("name"));

        // Get geo data
        JSONObject geo = restaurant.getJSONObject("geo");
        JSONArray coordinates = geo.getJSONArray("coordinates");

        // Prepare location
        Location location = new Location("");
        location.setLatitude(coordinates.getDouble(1));
        location.setLongitude(coordinates.getDouble(0));

        // Set location
        card.setLocation(location);

        // Set distance
        card.setDistance(restaurant.getDouble("distance"));

        // Set image name
        card.setImageName(dish.getJSONObject("media").getString("file"));

        return card;
    }

    //JSONObject media = dish.getJSONObject("media");
    //String fileName = media.getString("file");
    //URL imageUrl = getFullUrl("restaurants/media/" + fileName);

    //HttpConnectionHelper.downloadImage(this, imageUrl, fileName);

    //Drawable image = loadImage(directory, fileName);

    //Card card = new Card(_id, location, null, name, locationName, 0, 0);
    //card.setLocationId(locationReference);
    //card.setImageName(fileName);
}
