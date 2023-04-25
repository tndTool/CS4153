package com.cs4153;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class SignInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        getSupportActionBar().hide();
    }
}