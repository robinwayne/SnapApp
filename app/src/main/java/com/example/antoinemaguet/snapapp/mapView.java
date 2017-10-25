package com.example.antoinemaguet.snapapp;

import android.content.Context;
import android.location.LocationManager;
import android.location.Location;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;


public class mapView extends Fragment {

    private MapView mapView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_map_view, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Mapbox.getInstance(getContext(), BuildConfig.MAP_BOX_TOKEN);
        mapView = view.findViewById(R.id.mapView);
        mapView.setStyleUrl("https://tile.jawg.io/jawg-streets.json?access-token=" + BuildConfig.JAWG_API_KEY);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                // One way to add a marker view
                //mapboxMap.addMarker(new MarkerOptions().position(new LatLng(45.784265,4.869752)));
                Location location = new Location("service Provider");

                LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

                String NetProvider = LocationManager.NETWORK_PROVIDER;
                String GPSProvider = LocationManager.GPS_PROVIDER;

                try {
                    Location NetLocation = locationManager.getLastKnownLocation(NetProvider);
                    Location GPSLocation = locationManager.getLastKnownLocation(GPSProvider);
                    mapboxMap.addMarker(new MarkerOptions().position(new LatLng(NetLocation)));
                    mapboxMap.addMarker(new MarkerOptions().position(new LatLng(GPSLocation)));
                } catch (SecurityException e) {
                }




            }
        });
        // Inflate the layout for this fragment
    }
}
