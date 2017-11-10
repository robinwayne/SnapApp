package com.example.antoinemaguet.snapapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import org.json.JSONObject;

public class MapFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener{

    private GoogleApiClient googleApiClient;
    private MapView mapView;
    private LocationRequest locationRequest;
    private Marker currentPos;
    private Location mLastLocation;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG = "dialog";
    private DriveResourceClient mDriveResourceClient;
    private JSONObject jsonStories;
    private Icon icon;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map_view, container, false);

        googleApiClient
                = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Mapbox.getInstance(getContext(), BuildConfig.MAP_BOX_TOKEN);
        mapView = view.findViewById(R.id.mapView);
        mapView.setStyleUrl("https://tile.jawg.io/jawg-streets.json?access-token=" + BuildConfig.JAWG_API_KEY);
        mapView.onCreate(savedInstanceState);

        googleApiClient.connect();

        locationRequest = new LocationRequest()
                .setInterval(1000)
                .setPriority(
                        LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        FloatingActionButton FAB = (FloatingActionButton) view.findViewById(R.id.positionBtn);
        FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mLastLocation != null) {
                    mapView.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(MapboxMap mapboxMap) {

                            mapboxMap.easeCamera(new CameraUpdate() {
                                @Nullable
                                @Override
                                public CameraPosition getCameraPosition(@NonNull MapboxMap mapboxMap) {
                                    return new CameraPosition.Builder().target(new LatLng(mLastLocation)).zoom(15).bearing(0).build();
                                }
                            }, 2000);


                        }
                    });
                }
            }
        });
        Log.i("BLABLA","ViewCreated");


    }

    @Override
    public void onConnectionFailed(ConnectionResult result)
    {
        // This callback is important for handling errors
        // that may occur while attempting to connect with
        // Google.
        //
        // If your app won't work properly without location
        // updates, then it's important to handle connection
        // errors properly. See
        // https://developer.android.com/google/auth/api-client.html
        // for more information about handling errors when
        // connecting to Google.
        //
        // Location information is optional for this app, so
        // connection errors can be safely ignored here.

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // Connected to Google Play services!
        // This is where you will start asking for location
        // updates.
        //LocationServices.FusedLocationApi.requestLocationUpdates(
               // googleApiClient, locationRequest, this);
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            return;
        }

        LocationManager mLocationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        MapLocationListener mLocationListener = new MapLocationListener(mapView, currentPos, getActivity(), LoginActivity.jsonStories);

        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, mLocationListener);
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);

        if(mLastLocation != null) {
            this.mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(MapboxMap mapboxMap) {
                    mapboxMap.easeCamera(new CameraUpdate() {
                        @Nullable
                        @Override
                        public CameraPosition getCameraPosition(@NonNull MapboxMap mapboxMap) {
                            return new CameraPosition.Builder().target(new LatLng(mLastLocation)).zoom(15).bearing(0).build();
                        }
                    }, 2000);


                }
            });
        }



    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The Google Play connection has been interrupted.
        // Disable any UI components that depend on Google
        // APIs until onConnected() is called.
        //
        // This example doesn't need to do anything here.
    }

    private void requestLocationPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            new Camera2BasicFragment.ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Camera2BasicFragment.ErrorDialog.newInstance(getString(R.string.request_permission))
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }





}





