package com.example.firebase2019;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {
    EditText e1, e2;
    FirebaseAuth auth;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseUser user;
    SignInButton signInButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        e1 = (EditText) findViewById(R.id.editText3);
        e2 = (EditText) findViewById(R.id.editText4);
        auth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
        signInButton = (SignInButton)findViewById(R.id.mybutton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, 101);
            }
        });



    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 101) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                //Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();
                            Intent i = new Intent(getApplicationContext(),ProfileActivity.class);
                            startActivity(i);
                            finish();
                            Toast.makeText(getApplicationContext(),"User logged in successfully",Toast.LENGTH_SHORT).show();


                        } else {
                            Toast.makeText(getApplicationContext(),"Could not sign in user",Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });


    }

    public void loginUser(View v){
        if(e1.getText().toString().equals("") && e2.getText().toString().equals(""))
        {

            Toast.makeText(getApplicationContext(),"Blank Text not allowed",Toast.LENGTH_SHORT).show();

        }
        else
        {
            auth.signInWithEmailAndPassword(e1.getText().toString(),e2.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(getApplicationContext(),"User logged in successfully",Toast.LENGTH_SHORT).show();
                                finish();
                                Intent i = new Intent(getApplicationContext(),ProfileActivity.class);
                                startActivity(i);

                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(),"User could not be logged in",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }
    }

}
