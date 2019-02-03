package com.example.firebase2019;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void openRegister(View v){

        Intent i  = new Intent (this, RegisterActivity.class);
        startActivity(i);
    }

    public void openLogin(View v){

        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }


}



