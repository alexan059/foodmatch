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
    private String locationName;
    private double distance;
    private int pricing;

    // Attached information
    private Location location;
    private String reference;
    private String locationReference;
    private String imageName;

    public Card(String reference, Location location, Drawable image, String dish, String locationName, double distance, int pricing) {
        this.reference = reference;
        this.location = location;
        this.image = image;
        this.dish = dish;
        this.locationName = locationName;
        this.distance = distance;
        this.pricing = pricing;
    }

    public Card(String reference, Location location, Context context, int imageId, String dish, String locationName, double distance, int pricing) {
        this.reference = reference;
        this.location = location;
        this.image = ContextCompat.getDrawable(context, imageId);
        this.dish = dish;
        this.locationName = locationName;

        this.distance = distance;
        this.pricing = pricing;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getDish() {
        return dish;
    }

    public void setDish(String dish) {
        this.dish = dish;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
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

    public String getLocationReference() {
        return locationReference;
    }

    public void setLocationReference(String locationReference) {
        this.locationReference = locationReference;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
