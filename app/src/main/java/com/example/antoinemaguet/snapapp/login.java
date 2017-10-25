package com.example.antoinemaguet.snapapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button submitBtn = (Button) findViewById(R.id.submitBtn);
        Button resetBtn = (Button) findViewById(R.id.resetBtn);

        final EditText usernameEdit = (EditText) findViewById(R.id.usernameEdit);
        final EditText passwordEdit = (EditText) findViewById(R.id.passwordEdit);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//               new LoginTask.execute(
//                       .usernameEdit
//             );
                Intent intent = new Intent(login.this, Map.class);
                startActivity(intent);
            }
        });


        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(login.this, "Clear Inputs", Toast.LENGTH_LONG).show();
                usernameEdit.setText("");
                passwordEdit.setText("");
            }

        });
    }
}