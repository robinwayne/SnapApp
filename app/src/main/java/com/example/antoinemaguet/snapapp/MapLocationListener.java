package com.example.antoinemaguet.snapapp;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;


/**
 * Created by antoinemaguet on 29/10/2017.
 */


public class MapLocationListener implements LocationListener {

    private MapView mapView;
    private Marker currentPos;
    private Activity activity;
    private JSONObject jsonStories;

    public static Location lastLoc;
    MapLocationListener(MapView mapView, Marker pos, Activity activity, JSONObject jsonStories) {
        this.mapView=mapView;
        this.currentPos=pos;
        this.activity=activity;
        this.jsonStories=jsonStories;
    }

    @Override
    public void onLocationChanged(final Location location)
    {
        lastLoc=location;
        final JSONObject jsonDisplay=MapFragment.jsonCloseStories;
        Log.i("BLABLA","LocationChanged"+jsonDisplay);

        IconFactory iconFactory =IconFactory.getInstance(this.activity);
        final Icon icon = iconFactory.fromResource(R.drawable.ic_blue_marker);
        Log.i("BLABLA","LocationChanged");

        this.mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mapboxMap.clear();
                    mapboxMap.addMarker(new MarkerOptions().position(new LatLng(location)));
                    mapboxMap.addMarker(new MarkerOptions().position(new LatLng(location)));
                if(jsonDisplay != null) {
                    try {
                        JSONArray arr = jsonDisplay.getJSONArray("datas");
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject jsonObj = arr.getJSONObject(i);
                            Location tempLoc = new Location("tempLoc");
                            tempLoc.setLongitude(jsonObj.getDouble("longitude"));
                            tempLoc.setLatitude(jsonObj.getDouble("latitude"));
                            Float distance = location.distanceTo(tempLoc);
                            mapboxMap.addMarker(new MarkerOptions().position(new LatLng(tempLoc)).icon(icon));
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }


            }
        });
    }



}