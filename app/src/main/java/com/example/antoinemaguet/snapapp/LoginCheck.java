package com.example.antoinemaguet.snapapp;


import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

/**
 * Created by antoinemaguet on 03/11/2017.
 */

public class LoginCheck extends AsyncTask<Void, Void, Boolean> {

    private String username;
    private String password;
    private final LoginListener loginListener;
    private final ProgressBar progressBar;

    LoginCheck(String username, String password, ProgressBar progress, LoginListener loginListener) {
        this.username=username;
        this.password=password;
        this.loginListener=loginListener;
        this.progressBar=progress;

    }

    @Override
    protected void onPreExecute() {
        // Show Progress bar
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean connection=false;
        if("".equals(username)&&"".equals(password)) {
            connection=true;
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return connection;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        //Hide progress bar
        //Change activity or display error message
        if(success){
            loginListener.OnSuccess();
            //Intent intent = new Intent(LoginActivity.this, MyActivity.class);

        }else {
            loginListener.OnFailure();
        }
        progressBar.setVisibility(View.GONE);
    }

    public interface LoginListener{

        void OnSuccess();
        void OnFailure();

    }

}
