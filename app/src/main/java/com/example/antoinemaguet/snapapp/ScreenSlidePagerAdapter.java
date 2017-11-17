package com.example.antoinemaguet.snapapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.antoinemaguet.snapapp.Camera2BasicFragment;

/**
 * Created by antoinemaguet on 10/11/2017.
 */

public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

    private static final int NUM_PAGES = 3;
    public ScreenSlidePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                return new MapFragment();
            case 1:
                return new Camera2BasicFragment();
            case 2:
                return new StoryFragment();
            default:
                return new MapFragment();
        }
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }
}