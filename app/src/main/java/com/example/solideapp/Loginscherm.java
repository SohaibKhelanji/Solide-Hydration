package com.example.solideapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


public class Loginscherm extends AppCompatActivity {

    Button btnLogin, btnRegister;
    EditText email, wachtwoord;
    ApiManager apiManager;

    BluetoothConnection bluetoothConnection;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginscherm);

        apiManager = new ApiManager(this);
        bluetoothConnection = new BluetoothConnection(this);
        btnLogin = findViewById(R.id.btn1);
        btnRegister = findViewById(R.id.btn2);
        email = findViewById(R.id.email);
        wachtwoord = findViewById(R.id.wachtwoord);


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailString = email.getText().toString();
                String wachtwoordString = wachtwoord.getText().toString();
                if (emailadressvalid() && passwordvalid()) {

                    boolean isConnectedToInternet = apiManager.isConnectedToInternet(getApplicationContext());

                    if(isConnectedToInternet) {
                        getLoginStatus(emailString, wachtwoordString);

                    }else {
                        Toast.makeText(getApplicationContext(), "App heeft netwerkverbinding nodig om te functioneren, controleer de netwerk instellingen en probeer het nogmaals", Toast.LENGTH_LONG).show();
                    }



                }

                if(!emailadressvalid()){
                    Toast.makeText(getApplicationContext(), "Email mag niet leeg zijn of bevat invalide symbolen", Toast.LENGTH_LONG).show();
                }

                if(!passwordvalid()){
                    Toast.makeText(getApplicationContext(), "Wachtwoord mag niet leeg zijn", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

        public boolean emailadressvalid () {
            String emailString = email.getText().toString();
            String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

            if (emailString.matches(emailPattern)) {
                return true;
            } else {
                return false;
            }
        }
        public boolean passwordvalid () {
            String wachtwoordString = wachtwoord.getText().toString();
            if (wachtwoordString.equals("")) {
                return false;
            } else {
                return true;
            }
        }

    private void getLoginStatus(String emailString, String wachtwoordString) {
        apiManager.loginUser(emailString, wachtwoordString, new ApiManager.ApiResponseListener() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    JSONObject dataObject = response.getJSONObject("data");
                    String status = dataObject.getString("result");
                    int userId = dataObject.getInt("user");

                    Log.d("ResponseStatus", status);
                    Log.d("UserId", String.valueOf(userId));

                    if (status.equals("login successful")) {
                        Toast.makeText(getApplicationContext(), "Sucsessvol ingelogd", Toast.LENGTH_LONG).show();

                        // Pass user ID to the new activity
                        Intent intent = new Intent(getBaseContext(), Homepage.class);
                        intent.putExtra("userId", userId);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), "verkeerde ingloggegevens. controleer de gegevens en probeer het nogmaals", Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getApplicationContext(), "verkeerde ingloggegevens. controleer de gegevens en probeer het nogmaals", Toast.LENGTH_LONG).show();
                Log.e("ApiManager", "API Error: " + errorMessage);
            }
        });

    }
    }

