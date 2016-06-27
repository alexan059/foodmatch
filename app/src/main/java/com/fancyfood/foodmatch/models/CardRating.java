package com.fancyfood.foodmatch.models;

public class CardRating {

    private String _id;
    private boolean rating;
    private String timestamp;

    public CardRating(String _id, boolean rating, String timestamp) {
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
