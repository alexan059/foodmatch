package com.fancyfood.foodmatch.models;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

/**
 * Cards Model.
 */
public class Card {

    private Drawable image;
    private String dish;
    private String location;
    private double distance;
    private int pricing;

    public Card(Drawable image, String dish, String location, double distance, int pricing) {
        this.image = image;
        this.dish = dish;
        this.location = location;
        this.distance = distance;
        this.pricing = pricing;
    }

    public Card(Context context, int imageId, String dish, String location, double distance, int pricing) {
        this.image = ContextCompat.getDrawable(context, imageId);
        this.dish = dish;
        this.location = location;
        this.distance = distance;
        this.pricing = pricing;
    }

    public String getPricingDollars() {
        String str = "";
        for (int i = 0; i < this.pricing; i++) {
            str += "$";
        }

        return str;
    }

    public String getDish() {
        return dish;
    }

    public void setDish(String dish) {
        this.dish = dish;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getPricing() {
        return pricing;
    }

    public void setPricing(int pricing) {
        this.pricing = pricing;
    }

    public Drawable getImage() {
        return image;
    }

    public void setImage(Drawable image) {
        this.image = image;
    }
}
