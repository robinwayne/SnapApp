package com.example.antoinemaguet.snapapp;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.Executor;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

/**
 * Created by antoinemaguet on 11/11/2017.
 */

public class NewGoogleApiClient {
    private GoogleSignInClient mGoogleSignInClient;
    private DriveResourceClient mDriveResourceClient;
    public static JSONObject jsonStories;
    private GoogleSignInClient signInClient;
    NewGoogleApiClient(GoogleSignInClient signInClient){
        this.signInClient=signInClient;
    }


    public void signIn() {
        Log.i("BLABLA", "Start Connection");

        //GoogleSignInClient signInClient = buildGoogleSignInClient();
        Log.i("BLABLA", "client cree");
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
                        // startActivityForResult(signInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
                        Log.i("BLABLA", "Failed Connection");

                    }
                });

    }

    private void onSignInSuccess(GoogleSignInAccount account) {
        createDriveResourceClient(account);
        //createFileInAppFolder();
        //readStoriesFile();
    }

    private void createDriveResourceClient(GoogleSignInAccount account) {
        mDriveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), account);
    }

    protected DriveResourceClient getDriveResourceClient() {
        return mDriveResourceClient;
    }

    protected JSONObject readStoriesFile() {

        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, "Datas.json"))
                .build();
        Task<MetadataBuffer> queryTask = getDriveResourceClient().query(query);
        queryTask
                .addOnSuccessListener((Executor)this,
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
                .addOnFailureListener((Executor) this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure...
                    }
                });

        return jsonStories;

    }


}
