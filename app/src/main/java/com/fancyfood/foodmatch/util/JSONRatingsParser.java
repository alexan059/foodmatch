package com.fancyfood.foodmatch.util;

import android.location.Location;
import android.util.Log;

import com.fancyfood.foodmatch.models.Card;
import com.fancyfood.foodmatch.models.Rating;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public final class JSONRatingsParser {

    private static final String TAG = JSONRatingsParser.class.getSimpleName();

    public static JSONArray createJSONArray(ArrayList<Rating> ratings) throws JSONException {
        JSONArray array = new JSONArray();

        for (Rating rating : ratings) {
            array.put(ratingToJSON(rating));
        }

        return array;
    }

    public static JSONObject ratingToJSON(Rating rating) throws JSONException {
        JSONObject object = new JSONObject();
        JSONArray geo = new JSONArray();

        geo.put(rating.getLocation().getLongitude());
        geo.put(rating.getLocation().getLatitude());

        object.put("rating", (rating.getRating()) ? 1 : 0);
        object.put("geo", geo);
        object.put("dish_id", rating.getReference());
        object.put("created_at", rating.getTimestamp());

        return object;
    }

}
