package com.example.antoinemaguet.snapapp;

/**
 * Created by antoinemaguet on 03/11/2017.
 */

public class ListObjectRecyclerView {
    private String text;
    private String imagePath;

    public ListObjectRecyclerView(String text, String imageUrl) {
        this.text = text;
        this.imagePath = imageUrl;
    }
    public ListObjectRecyclerView(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
