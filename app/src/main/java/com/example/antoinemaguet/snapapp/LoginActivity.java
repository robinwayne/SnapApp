package com.example.antoinemaguet.snapapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;

public class LoginActivity extends AppCompatActivity {


    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int REQUEST_CODE_SIGN_IN = 0;
    private static final String DRIVE_ID = "driveId";
    public static DriveResourceClient mDriveResourceClient;
    public static JSONObject jsonStories;
    private GoogleSignInClient signInClientTest;
    public static Activity activity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button submitBtn = (Button) findViewById(R.id.submitBtn);
        Button resetBtn = (Button) findViewById(R.id.resetBtn);

        if(!checkPermission()) {
            requestPermission();
        }

        //mGoogleSignInClient = buildGoogleSignInClient();
        //signInClientTest=buildGoogleSignInClient();
        //NewGoogleApiClient mGoogleApiClient = new NewGoogleApiClient(signInClientTest);
        //mGoogleSignInClient = mGoogleApiClient.buildGoogleSignInClient();

        //GoogleSignInClient signInClient = mGoogleApiClient.buildGoogleSignInClient();
        //mGoogleApiClient.signIn();
        //JSONObject jsontest = mGoogleApiClient.readStoriesFile();
        //Log.i("BLABLA","messagecree par classe"+jsontest);
        final EditText usernameEdit = (EditText) findViewById(R.id.usernameEdit);
        final EditText passwordEdit = (EditText) findViewById(R.id.passwordEdit);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LoginCheck( usernameEdit.getText().toString(),passwordEdit.getText().toString(),(ProgressBar)findViewById(R.id.progressBar),new LoginCheck.LoginListener() {
                    @Override
                    public void OnSuccess() {
                        Intent intent = new Intent(LoginActivity.this, FragmentsActivity.class);
                        startActivity(intent);
                    }
                    public void OnFailure(){
                        Toast.makeText(getBaseContext(), R.string.wrongPassword, Toast.LENGTH_LONG).show();
                    }
                }).execute();

            }
        });


        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(LoginActivity.this, "Clear Inputs", Toast.LENGTH_LONG).show();
                usernameEdit.setText("");
                passwordEdit.setText("");
            }

        });


        signIn();


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
        activity=this;
        return GoogleSignIn.getClient(activity, signInOptions);

    }

    private void createDriveResourceClient(GoogleSignInAccount account) {
        mDriveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), account);
    }


    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (!locationAccepted && cameraAccepted)
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }


                break;
        }
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(LoginActivity.this)
                .setMessage(message)
                .setPositiveButton("Accept", okListener)
                .setNegativeButton("Deny", null)
                .create()
                .show();
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

                        JSONObject jo1 = new JSONObject();
                        jo1.put("latitude", "45.80");
                        jo1.put("longitude", "4.84");

                        JSONObject jo2 = new JSONObject();
                        jo2.put("latitude", "45.81");
                        jo2.put("longitude", "4.85");

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
                                .setTitle("Tester.json")
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

}