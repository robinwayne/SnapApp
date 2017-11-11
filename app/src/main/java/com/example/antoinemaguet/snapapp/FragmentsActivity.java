package com.example.antoinemaguet.snapapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class FragmentsActivity extends FragmentActivity {

    private PagerAdapter mPagerAdapter;
    private ViewPager vPager;

    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int REQUEST_CODE_SIGN_IN = 1;

    public static DriveResourceClient mDriveResourceClient;
    public static JSONObject jsonStories;
    public static Activity activity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragments_activity); // The view
        activity=this;

        // Instantiate a ViewPager and a PagerAdapter.
        signIn();
        vPager = (ViewPager) findViewById(R.id.viewpager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        vPager.setAdapter(mPagerAdapter);

        
    }




    @Override
    public void onBackPressed() {
        if (vPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            vPager.setCurrentItem(vPager.getCurrentItem() - 1);
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
    }


    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_APPFOLDER)
                        .build();
        return GoogleSignIn.getClient(this, signInOptions);

    }

    private void createDriveResourceClient(GoogleSignInAccount account) {
        mDriveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), account);
    }


    private void createFileInAppFolder() {

        final Task<DriveFolder> appFolderTask = getDriveResourceClient().getAppFolder();
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
                .addOnSuccessListener(this,
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
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("BLABLA", "Unable to create file");
                    }
                });

    }


    private void readStoriesFile() {

        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, "Datas.json"))
                .build();
        Task<MetadataBuffer> queryTask = getDriveResourceClient().query(query);
        queryTask
                .addOnSuccessListener(this,
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
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure...
                    }
                });

    }

    protected DriveResourceClient getDriveResourceClient() {
        return mDriveResourceClient;
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */



}


