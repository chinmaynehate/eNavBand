package com.example.firebase2019;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.w3c.dom.Text;

import cz.msebera.android.httpclient.Header;

public class ProfileActivity extends AppCompatActivity {


    FirebaseAuth auth;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        Button button7 = (Button)findViewById(R.id.button7);
        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickR(v);
            }


        });



        Button button6 = (Button)findViewById(R.id.button6);
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickL(v);
            }


        });

    }

    public void signOut(View v){
        auth.signOut();
        finish();
        Intent i =new Intent(this, MainActivity.class);
        startActivity(i);
        Toast.makeText(getApplicationContext(), "Logged Out Successfully", Toast.LENGTH_SHORT).show();



    }

    public void onClickR(final View v){
        v.setEnabled(false);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get("https://jsonplaceholder.typicode.com/users", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(responseBody != null){
                    TextView txtResponse = (TextView)findViewById(R.id.textView10);
                    assert txtResponse != null;
                    txtResponse.setText(new String(responseBody));
                }
                v.setEnabled(true);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                v.setEnabled(true);
            }
        });




    }



    public void onClickL(final View v){
        v.setEnabled(false);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://free.currencyconverterapi.com/api/v5/convert?q=EUR_USD&compact=y", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(responseBody != null){
                    TextView txtResponse = (TextView)findViewById(R.id.textView11);
                    assert txtResponse != null;
                    txtResponse.setText(new String(responseBody));
                }
                v.setEnabled(true);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                v.setEnabled(true);
            }
        });




    }
}
