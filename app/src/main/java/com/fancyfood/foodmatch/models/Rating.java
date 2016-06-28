package com.fancyfood.foodmatch.models;

import android.location.Location;

public class Rating {

    private String _id;
    private boolean rating;
    private String timestamp;
    private Location location;

    public Rating(String _id, boolean rating, Location location, String timestamp) {
        this._id = _id;
        this.rating = rating;
        this.timestamp = timestamp;
    }

    public String getID() {
        return _id;
    }

    public void setID(String _id) {
        this._id = _id;
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
