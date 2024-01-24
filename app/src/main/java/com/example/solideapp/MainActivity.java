package com.example.solideapp;

import androidx.appcompat.app.AppCompatActivity;

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


public class  MainActivity extends AppCompatActivity {

    Button btn, btnlogin;
    EditText email, username, wachtwoord;

    ApiManager apiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiManager = new ApiManager(this);
        btn = findViewById(R.id.btnRegister);
        btnlogin = findViewById(R.id.buttonlogin);
        email = findViewById(R.id.email);
        username = findViewById(R.id.username);
        wachtwoord = findViewById(R.id.wachtwoord);

        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), Loginscherm.class);
                startActivity(intent);
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailString = email.getText().toString();
                String usernameString = username.getText().toString();
                String wachtwoordString = wachtwoord.getText().toString();
                if (emailadressvalid() && usernamevalid() && passwordvalid()) {

                    getRegisterStatus(usernameString, emailString, wachtwoordString);

                }

                if (!emailadressvalid()) {
                    Toast.makeText(getApplicationContext(), "Email mag niet leeg zijn of bevat invalide symbolen", Toast.LENGTH_LONG).show();
                }

                if (!usernamevalid()) {
                    Toast.makeText(getApplicationContext(), "Gebruikersnaam mag niet leeg zijn", Toast.LENGTH_LONG).show();
                }

                if (!passwordvalid()) {
                    Toast.makeText(getApplicationContext(), "Wachtwoord mag niet leeg zijn", Toast.LENGTH_LONG).show();
                }
            }

            public boolean emailadressvalid() {
                String emailString = email.getText().toString();
                String emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z]+\\.+[a-zA-Z]+";

                if (emailString.matches(emailPattern)) {
                    return true;
                } else {
                    return false;
                }
            }

            public boolean usernamevalid() {
                String usernameString = username.getText().toString();
                if (usernameString.equals("")) {
                    return false;
                } else {
                    return true;
                }
            }

            public boolean passwordvalid() {
                String wachtwoordString = wachtwoord.getText().toString();
                if (wachtwoordString.equals("")) {
                    return false;
                } else {
                    return true;
                }
            }


            private void getRegisterStatus(String usernameString, String emailString, String wachtwoordString) {
                apiManager.registerUser(usernameString, emailString, wachtwoordString, new ApiManager.ApiResponseListener() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        try {
                            if (response.has("error") && response.getString("error").equals("Email already exists")) {
                                // Email already exists
                                Toast.makeText(getApplicationContext(), "Email already exists. Please use a different email.", Toast.LENGTH_LONG).show();
                            } else if (response.has("data") && response.getString("data").equals("user created")) {
                                // User created successfully
                                Toast.makeText(getApplicationContext(), "Successfully registered", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getBaseContext(), Loginscherm.class);
                                startActivity(intent);
                            } else {
                                // Handle other cases or unexpected response
                                Toast.makeText(getApplicationContext(), "An error occurred during registration. Please try again.", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // Handle error here, such as displaying an error message
                        if (errorMessage != null) {
                            Log.e("ApiManager", "API Error: " + errorMessage);

                            if (errorMessage.toLowerCase().contains("email") && errorMessage.toLowerCase().contains("exists")) {
                                // Email already exists
                                Toast.makeText(MainActivity.this, "Email already exists. Please use a different email.", Toast.LENGTH_LONG).show();
                            } else {
                                // Handle other errors
                                Toast.makeText(MainActivity.this, "Error registering user: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("ApiManager", "API Error: Unknown error");
                            Toast.makeText(MainActivity.this, "Email is al ingebruik, gelieve inloggen of een andere email gebruiken, en nogmaals proberen", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}