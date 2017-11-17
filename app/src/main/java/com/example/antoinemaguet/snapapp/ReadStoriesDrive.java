package com.example.antoinemaguet.snapapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

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
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.android.gms.drive.query.Filter;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by antoinemaguet on 17/11/2017.
 */

public class ReadStoriesDrive{

    private Activity activity;
    private Context context;
    public ReadStoriesDrive(Activity activity, Context context){
        this.activity=activity;
        this.context=context;
    }
    private JSONArray ja = new JSONArray();
    public static List<ListObjectRecyclerView> myDataSet = new ArrayList<>();

    private static final int REQUEST_CODE_SIGN_IN = 1;
    private MetadataBuffer metadataBuffer;

    public static DriveResourceClient mDriveResourceClient;
    public static JSONObject jsonStories;
    public static JSONObject jsonCloseStories;

    public void signIn() {
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
                        activity.startActivityForResult(signInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
                        Log.i("BLABLA", "Failed Connection");

                    }
                });

    }

    private void onSignInSuccess(GoogleSignInAccount account) {
        createDriveResourceClient(account);
        //createFileInAppFolder();
        //readStoriesFile();
        //createFolder();
        //listFiles();
        closestStories();
    }


    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_APPFOLDER)
                        .requestScopes(Drive.SCOPE_FILE)
                        .build();
        return GoogleSignIn.getClient(activity, signInOptions);

    }

    private void createDriveResourceClient(GoogleSignInAccount account) {
        mDriveResourceClient = Drive.getDriveResourceClient(context, account);
    }

    public void closestStories() {

        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, "Datas.json"))
                .build();
        Task<MetadataBuffer> queryTask = getDriveResourceClient().query(query);
        queryTask
                .addOnSuccessListener(activity,
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
                                                    jsonCloseStories = getCloseStories(MapLocationListener.lastLoc,jsonStories);
                                                    loadPictures();
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
                .addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure...
                    }
                });



    }

    private void loadPictures() {

        Query query2;
        Filter test = null;

        if (jsonCloseStories != null) {

            try {
                JSONArray arr = jsonCloseStories.getJSONArray("datas");

                for(int i=0;i<arr.length();i++){
                    if(arr.getJSONObject(i) != null) {
                        Filter teste = null;
                        teste = Filters.eq(SearchableField.TITLE, arr.getJSONObject(i).get("imageTitle").toString());
                        if(i>0){
                            test = Filters.or(test, Filters.eq(SearchableField.TITLE, arr.getJSONObject(i).get("imageTitle").toString()));
                        }else{
                            test=teste;
                        }
                    }
                }


            }catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        Log.i("BLABLAB", "JSON debug "+test);
        query2 = new Query.Builder()
                .addFilter(test)
                .build();


        // [START query_files]
        Task<MetadataBuffer> queryTask = getDriveResourceClient().query(query2);
        // [END query_files]


        // [START query_results]
        queryTask
                .addOnSuccessListener(
                        new OnSuccessListener<MetadataBuffer>() {
                            @Override
                            public void onSuccess(MetadataBuffer metadataBufferBis) {
                                metadataBuffer=metadataBufferBis;
                                // Handle results...
                                // [START_EXCLUDE]
                                JSONObject mainObj = new JSONObject();
                                final BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inSampleSize = 4;
                                myDataSet.clear();
                                for (Iterator<Metadata> i = metadataBuffer.iterator(); i.hasNext();) {
                                    Metadata item = i.next();

                                    //get the title
                                    //final String title = item.getTitle();
                                    //final String longitude = item.getTitle();
                                    final String latitude = item.getCustomProperties().get(new CustomPropertyKey("Latitude", CustomPropertyKey.PUBLIC));
                                    final String longitude = item.getCustomProperties().get(new CustomPropertyKey("Longitude", CustomPropertyKey.PUBLIC));

                                    Log.i("BLABLA","Latitude est :"+latitude);
                                    //get the description
                                    final String description;
                                    if (item.getDescription() != null) {
                                        description = item.getDescription();
                                    }
                                    else{
                                        description = "";
                                    }
                                    try {
                                        JSONObject jo = new JSONObject();
                                        jo.put("latitude", latitude);
                                        jo.put("longitude", longitude);
                                        jo.put("text", description);
                                        ja.put(jo);
                                        mainObj.put("datas", ja);
                                        Log.i("BLABLA", "json file"+ mainObj.toString() );
                                    }catch (JSONException e) {

                                    }
                                    //get the picture
                                    DriveId mId = item.getDriveId();
                                    DriveFile file = mId.asDriveFile();

                                    Task<DriveContents> openFileTask =
                                            getDriveResourceClient().openFile(file, DriveFile.MODE_READ_ONLY);
                                    openFileTask
                                            .continueWithTask(new Continuation<DriveContents, Task<Void>>() {
                                                @Override
                                                public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {

                                                    DriveContents contents = task.getResult();
                                                    // Process contents...
                                                    InputStream mInputStream = contents.getInputStream();
                                                    Rect tester= new Rect();
                                                    Bitmap bitmap1 = BitmapFactory.decodeStream(mInputStream, null,options);

                                                    //add title description image to myDataSet
                                                    myDataSet.add(new ListObjectRecyclerView(description,bitmap1));
                                                    Log.i("BLABLAB", "JSON debug ");

                                                    Log.i("BLABLA","new object recycler view"+ description);
                                                    Task<Void> discardTask = getDriveResourceClient().discardContents(contents);
                                                    return discardTask;

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.i("BLABLA","faileddddd");
                                                }
                                            });
                                    //metadataBuffer.release();

                                }
                                //jsonStories=mainObj;

                                //metadataBuffer.release();
                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.i("BLABLA", "Error retrieving files");

                    }
                });
        // [END query_results]
    }

    private JSONObject getCloseStories(Location location, JSONObject jsonStories){


        JSONObject jsonTrans= new JSONObject();
        JSONArray ja = new JSONArray();
        if (location == null){
            location=MapLocationListener.lastLoc;
        }
        try{
            JSONArray arr = jsonStories.getJSONArray("datas");
            for(int i=0;i<arr.length();i++){

                if(arr.getJSONObject(i) != null) {
                    JSONObject jsonObj = arr.getJSONObject(i);
                    Location tempLoc = new Location("tempLoc");
                    tempLoc.setLongitude(Double.parseDouble(jsonObj.get("longitude").toString()));
                    tempLoc.setLatitude(Double.parseDouble(jsonObj.get("latitude").toString()));
                    Float distance = location.distanceTo(tempLoc);

                    if (distance < 5000) {
                        JSONObject field = arr.getJSONObject(i);
                        ja.put(field);
                        Log.i("BLABLA", "close debug " + field.get("imageTitle").toString());
                    }
                }
            }
            jsonTrans.put("datas", ja);
        }catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return jsonTrans;
    }

    protected DriveResourceClient getDriveResourceClient() {
        return mDriveResourceClient;
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
                .addOnSuccessListener(activity,
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
                .addOnFailureListener(activity, new OnFailureListener() {
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
                .addOnSuccessListener(activity,
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
                .addOnFailureListener(activity, new OnFailureListener() {
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
                .addOnSuccessListener(activity,
                        new OnSuccessListener<DriveFolder>() {
                            @Override
                            public void onSuccess(DriveFolder driveFolder) {
                                Log.i("BLABLA", "Folder created"+driveFolder.getDriveId().toString());


                            }
                        })
                .addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("BLABLA", "Unable to create folder");

                    }
                });
    }





}
