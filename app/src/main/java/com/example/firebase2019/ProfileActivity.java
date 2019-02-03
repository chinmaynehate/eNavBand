package com.example.firebase2019;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {


    FirebaseAuth auth;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();


    }

    public void signOut(View v){
        auth.signOut();
        finish();
        Intent i =new Intent(this, MainActivity.class);
        startActivity(i);
        Toast.makeText(getApplicationContext(), "Logged Out Successfully", Toast.LENGTH_SHORT).show();



    }
}
