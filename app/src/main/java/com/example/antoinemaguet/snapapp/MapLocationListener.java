package com.example.antoinemaguet.snapapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;


/**
 * Created by antoine on 29/10/2017.
 */


public class MapLocationListener implements LocationListener {

    private MapView mapView;
    private Marker currentPos;

    MapLocationListener(MapView mapView, Marker pos) {
        this.mapView=mapView;
        this.currentPos=pos;
    }

    @Override
    public void onLocationChanged(final Location location)
    {
        this.mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                if(currentPos==null) {
                    currentPos=mapboxMap.addMarker(new MarkerOptions().position(new LatLng(location)));
                }else{
                    currentPos.setPosition(new LatLng(location));
                }


            }
        });
    }




}