package com.example.antoinemaguet.snapapp;

import android.app.Activity;
import android.location.Location;
import android.util.Log;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by antoinemaguet on 10/11/2017.
 */

public class CloseStories {


    public List<ListObjectRecyclerView> getCloseStories(Location location, JSONObject jsonStories){
        List<ListObjectRecyclerView> myDataSet=new ArrayList<>();
        try{
            JSONArray arr = jsonStories.getJSONArray("datas");
            for(int i=0;i<arr.length();i++){
                JSONObject jsonObj = arr.getJSONObject(i);
                Location tempLoc= new Location("tempLoc");
                Float distance = location.distanceTo(tempLoc);
                if (distance > 5000){
                    JSONObject field = arr.getJSONObject(i);
                    myDataSet.add(new ListObjectRecyclerView(jsonObj.getString("text")));
                }
            }
        }catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return myDataSet;
    }

    public List<LatLng> getCloseStoriesCoord(Location location, JSONObject jsonStories){
        List<LatLng> myDataSet=new ArrayList<>();
        try{
            JSONArray arr = jsonStories.getJSONArray("datas");
            for(int i=0;i<arr.length();i++){
                JSONObject jsonObj = arr.getJSONObject(i);
                Location tempLoc= new Location("tempLoc");
                tempLoc.setLongitude(jsonObj.getDouble("longitude"));
                tempLoc.setLatitude(jsonObj.getDouble("latitude"));
                Float distance = location.distanceTo(tempLoc);
                if (distance < 5000){
                    myDataSet.add(new LatLng(tempLoc));
                }
            }
        }catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return myDataSet;
    }

}
