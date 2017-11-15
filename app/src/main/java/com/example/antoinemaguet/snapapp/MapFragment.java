package com.example.antoinemaguet.snapapp;

import android.Manifest;
import android.app.Activity;
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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class MapFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener{

    private GoogleApiClient googleApiClient;
    private MapView mapView;
    private LocationRequest locationRequest;
    private Marker currentPos;
    private Location mLastLocation;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG = "dialog";
    //private DriveResourceClient mDriveResourceClient;
   // private JSONObject jsonStories;
    private Icon icon;

    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int REQUEST_CODE_SIGN_IN = 1;

    public static DriveResourceClient mDriveResourceClient;
    public static JSONObject jsonStories;

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
        Object test = view.getTag();
            Log.i("LAA","Laa"+test);
        String tag = new String("tag");
        view.setTag(tag );
        Object test1 = view.getTag();
        Log.i("LAA","Laa"+test1);
        signIn();

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
        MapLocationListener mLocationListener = new MapLocationListener(mapView, currentPos, getActivity(), jsonStories);

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

    private void signIn() {
        final GoogleSignInClient signInClient = buildGoogleSignInClient();
        signInClient.silentSignIn()
                .addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                        onSignInSuccess(googleSignInAccount);
                        Log.i("BLABLA", "Success Connection");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Silent sign-in failed, display account selection prompt
                        startActivityForResult(signInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
                        Log.i("BLABLA", "Failed Connection");

                    }
                });

    }

    private void onSignInSuccess(GoogleSignInAccount account) {
        createDriveResourceClient(account);
        //createFileInAppFolder();
        readStoriesFile();
        //createFolder();
    }


    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_APPFOLDER)
                        .requestScopes(Drive.SCOPE_FILE)
                        .build();
        return GoogleSignIn.getClient(getActivity(), signInOptions);

    }

    private void createDriveResourceClient(GoogleSignInAccount account) {
        mDriveResourceClient = Drive.getDriveResourceClient(getContext(), account);
    }


    private void createFileInAppFolder() {

        final Task<DriveFolder> appFolderTask = getDriveResourceClient().getRootFolder();
        final Task<DriveContents> createContentsTask = getDriveResourceClient().createContents();

        Tasks.whenAll(appFolderTask, createContentsTask)
                .continueWithTask(new Continuation<Void, Task<DriveFile>>() {
                    @Override
                    public Task<DriveFile> then(@NonNull Task<Void> task) throws Exception {
                        DriveFolder parent = appFolderTask.getResult();

                        DriveContents contents = createContentsTask.getResult();

                        OutputStream outputStream = contents.getOutputStream();

                        JSONObject jo = new JSONObject();
                        jo.put("latitude", "45.79");
                        jo.put("longitude", "4.83");
                        jo.put("text", "essai1");

                        JSONObject jo1 = new JSONObject();
                        jo1.put("latitude", "45.80");
                        jo1.put("longitude", "4.84");
                        jo1.put("text", "essai2");

                        JSONObject jo2 = new JSONObject();
                        jo2.put("latitude", "45.81");
                        jo2.put("longitude", "4.85");
                        jo2.put("text", "essai3");

                        JSONArray ja = new JSONArray();
                        ja.put(jo);
                        ja.put(jo1);
                        ja.put(jo2);
                        JSONObject mainObj = new JSONObject();
                        mainObj.put("datas", ja);

                        try (Writer writer = new OutputStreamWriter(outputStream)) {
                            writer.write(mainObj.toString());
                        }

                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle("Datas.json")
                                .setMimeType("application/json")
                                .setStarred(true)
                                .build();

                        return getDriveResourceClient().createFile(parent, changeSet, contents);
                    }
                })
                .addOnSuccessListener(getActivity(),
                        new OnSuccessListener<DriveFile>() {
                            @Override
                            public void onSuccess(DriveFile driveFile) {
                                Log.i("BLABLA", "File created" + driveFile.getDriveId().encodeToString());
                                Task<DriveContents> openFileTask =
                                        getDriveResourceClient().openFile(driveFile, DriveFile.MODE_READ_ONLY);
                                openFileTask
                                        .continueWithTask(new Continuation<DriveContents, Task<Void>>() {
                                            @Override
                                            public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                                                DriveContents contents = task.getResult();
                                                // Process contents...
                                                try (BufferedReader reader = new BufferedReader(
                                                        new InputStreamReader(contents.getInputStream()))) {
                                                    StringBuilder builder = new StringBuilder();
                                                    String line;
                                                    while ((line = reader.readLine()) != null) {
                                                        builder.append(line).append("\n");
                                                    }
                                                    Log.i("BLABLA", "File created" +builder.toString());
                                                }
                                                Task<Void> discardTask = getDriveResourceClient().discardContents(contents);
                                                return discardTask;
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Handle failure
                                            }
                                        });


                            }
                        })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("BLABLA", "Unable to create file");
                    }
                });

    }


    public void readStoriesFile() {

        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, "Datas.json"))
                .build();
        Task<MetadataBuffer> queryTask = getDriveResourceClient().query(query);
        queryTask
                .addOnSuccessListener(getActivity(),
                        new OnSuccessListener<MetadataBuffer>() {
                            @Override
                            public void onSuccess(MetadataBuffer metadataBuffer) {
                                DriveId driveId = metadataBuffer.get(0).getDriveId();
                                DriveFile filerr = driveId.asDriveFile();
                                Task<DriveContents> openFileTask =
                                        getDriveResourceClient().openFile(filerr, DriveFile.MODE_READ_ONLY);
                                openFileTask
                                        .continueWithTask(new Continuation<DriveContents, Task<Void>>() {
                                            @Override
                                            public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                                                DriveContents contents = task.getResult();
                                                // Process contents...
                                                try (BufferedReader reader = new BufferedReader(
                                                        new InputStreamReader(contents.getInputStream()))) {
                                                    StringBuilder builder = new StringBuilder();
                                                    String line;
                                                    while ((line = reader.readLine()) != null) {
                                                        builder.append(line).append("\n");
                                                    }
                                                    Log.i("BLABLA", "File Readable" +builder.toString());
                                                    jsonStories = new JSONObject(builder.toString());

                                                }
                                                Task<Void> discardTask = getDriveResourceClient().discardContents(contents);
                                                return discardTask;
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Handle failure
                                            }
                                        });

                            }
                        })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure...
                    }
                });

    }

    private void createFolder() {


        getDriveResourceClient()
                .getRootFolder()
                .continueWithTask(new Continuation<DriveFolder, Task<DriveFolder>>() {
                    @Override
                    public Task<DriveFolder> then(@NonNull Task<DriveFolder> task)
                            throws Exception {
                        DriveFolder parentFolder = task.getResult();
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle("Images")
                                .setMimeType(DriveFolder.MIME_TYPE)
                                .setStarred(true)
                                .build();
                        return getDriveResourceClient().createFolder(parentFolder, changeSet);
                    }
                })
                .addOnSuccessListener(getActivity(),
                        new OnSuccessListener<DriveFolder>() {
                            @Override
                            public void onSuccess(DriveFolder driveFolder) {
                                Log.i("BLABLA", "Folder created"+driveFolder.getDriveId().toString());


                            }
                        })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("BLABLA", "Unable to create folder");

                    }
                });
    }

    protected DriveResourceClient getDriveResourceClient() {
        return mDriveResourceClient;
    }





}





