package com.fancyfood.foodmatch.models;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v4.content.ContextCompat;

/**
 * Cards Model.
 */
public class Card {

    // For Displaying Card
    private Drawable image;
    private String dish;
    private String location;
    private double distance;
    private int pricing;

    // Attached information
    private Location position;
    private String _id;

    public Card(String _id, Location position, Drawable image, String dish, String location, double distance, int pricing) {
        this._id = _id;
        this.position = position;
        this.image = image;
        this.dish = dish;
        this.location = location;
        this.distance = distance;
        this.pricing = pricing;
    }

    public Card(String _id, Location position, Context context, int imageId, String dish, String location, double distance, int pricing) {
        this._id = _id;
        this.position = position;
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

    public Location getPosition() {
        return position;
    }

    public void setPosition(Location position) {
        this.position = position;
    }

    public String getID() {
        return _id;
    }

    public void setID(String _id) {
        this._id = _id;
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
