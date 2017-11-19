package com.example.antoinemaguet.snapapp;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.location.Location;
import android.support.annotation.NonNull;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;

import com.google.android.gms.drive.query.Filter;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;


import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.InputStream;







public class MapLocationListener implements LocationListener  {

    private MapView mapView;
    private Activity activity;
    private JSONObject jsonStories;

    private MetadataBuffer metadataBuffer;



    public static Location lastLoc;

    MapLocationListener(MapView mapView, Activity activity, JSONObject jsonStories) {
        this.mapView=mapView;
        this.activity=activity;
        this.jsonStories=jsonStories;

    }


    @Override
    onLocationChanged(final Location location){
        lastLoc=location;
        Log.i("BLABLA", "json listener"+ ReadStoriesDrive.jsonCloseStories);
        final JSONObject jsonDisplay=ReadStoriesDrive.jsonCloseStories;
        IconFactory iconFactory =IconFactory.getInstance(this.activity);

        final Icon icon = iconFactory.fromResource(R.drawable.ic_blue_marker);


        this.mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                //Clear previous markers
                mapboxMap.clear();

                //NEw current location marker

                    final Marker myPos = mapboxMap.addMarker(new MarkerOptions().position(new LatLng(location)));
                if(jsonDisplay != null) {

                    try {
                        JSONArray arr = jsonDisplay.getJSONArray("datas");
                        //Regarde tous les coordonnes et affiche des markers pour chacun d'entre eux
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject jsonObj = arr.getJSONObject(i);
                            final Location tempLoc = new Location("tempLoc");
                            tempLoc.setLongitude(jsonObj.getDouble("longitude"));
                            tempLoc.setLatitude(jsonObj.getDouble("latitude"));
                            Float distance = location.distanceTo(tempLoc);

                            if(distance<5000){
                                mapboxMap.addMarker(new MarkerOptions().position(new LatLng(tempLoc)).icon(icon));
                            }



                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }


                }

                //Pour chaque marker
                mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                        boolean result = false;
                        final Dialog marker_popup = new Dialog(activity);
                        marker_popup.setCancelable(true);


                        final View view = activity.getLayoutInflater().inflate(R.layout.marker_popup, null, true);
                        marker_popup.setContentView(view);

                        final ImageView picture_story = (ImageView) view.findViewById(R.id.image_linked_marker);
                        final TextView description_story = (TextView) view.findViewById(R.id.marker_description);


                        Query query_picture_marker;
                        Filter test = null;

                        Double longitude_marker = marker.getPosition().getLongitude();
                        Double latitude_marker = marker.getPosition().getLatitude();

                        if (myPos != marker) {
                            try {
                                JSONArray arr = jsonDisplay.getJSONArray("datas");
                                for (int i = 0; i < arr.length(); i++) {
                                    if (arr.getJSONObject(i) != null) {

                                        if (Double.parseDouble(arr.getJSONObject(i).get("longitude").toString()) == longitude_marker &&
                                                Double.parseDouble(arr.getJSONObject(i).get("latitude").toString()) == latitude_marker) {

                                            test = Filters.eq(SearchableField.TITLE, arr.getJSONObject(i).get("imageTitle").toString());
                                            break;
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            query_picture_marker = new Query.Builder()
                                .addFilter(test)
                                .build();

                        Task<MetadataBuffer> queryTask = ReadStoriesDrive.mDriveResourceClient.query(query_picture_marker);

                        queryTask
                                .addOnSuccessListener(
                                        new OnSuccessListener<MetadataBuffer>() {
                                            @Override
                                            public void onSuccess(MetadataBuffer metadataBufferBis) {
                                                metadataBuffer = metadataBufferBis;
                                                final BitmapFactory.Options options = new BitmapFactory.Options();
                                                options.inSampleSize = 4;
                                                Metadata item = metadataBuffer.get(0);
                                                DriveId mId = item.getDriveId();
                                                DriveFile file = mId.asDriveFile();
                                                final String desc = item.getDescription();
                                                Task<DriveContents> openFileTask =
                                                        ReadStoriesDrive.mDriveResourceClient.openFile(file, DriveFile.MODE_READ_ONLY);
                                                openFileTask
                                                        .continueWithTask(new Continuation<DriveContents, Task<Void>>() {
                                                            @Override
                                                            public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                                                                DriveContents contents = task.getResult();
                                                                InputStream mInputStream = contents.getInputStream();
                                                                Bitmap bitmap1 = BitmapFactory.decodeStream(mInputStream, null, options);
                                                                description_story.setText(desc);
                                                                picture_story.setImageBitmap(bitmap1);
                                                                marker_popup.show();
                                                                Task<Void> discardTask = ReadStoriesDrive.mDriveResourceClient.discardContents(contents);
                                                                return discardTask;

                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                            }
                                                        });

                                                metadataBuffer.release();

                                            }
                                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Log.i("BLABLA", "Error retrieving files");

                                    }
                                });


                    }

                        return result;
                    }


                });
            }
        });
    }

}

