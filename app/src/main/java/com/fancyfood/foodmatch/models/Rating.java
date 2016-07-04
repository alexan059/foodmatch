package com.fancyfood.foodmatch.models;

import android.location.Location;

public class Rating {

    private String reference;
    private boolean rating;
    private String timestamp;
    private Location location;

    public Rating(String reference, boolean rating, Location location, String timestamp) {
        this.reference = reference;
        this.rating = rating;
        this.location = location;
        this.timestamp = timestamp;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public boolean getRating() {
        return rating;
    }

    public void setRating(boolean rating) {
        this.rating = rating;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
