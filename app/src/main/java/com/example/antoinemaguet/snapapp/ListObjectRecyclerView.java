package com.example.antoinemaguet.snapapp;

import android.graphics.Bitmap;

/**
 * Created by antoinemaguet on 03/11/2017.
 */

public class ListObjectRecyclerView {
    private String text;
    private Bitmap image;

    public ListObjectRecyclerView(String text, Bitmap image) {
        this.text = text;
        this.image = image;
    }

    public String getText() {
        return text;
    }

    public Bitmap getImagePath() {
        return image;
    }

    public void setText(String text) {
        this.text = text;
    }

}
