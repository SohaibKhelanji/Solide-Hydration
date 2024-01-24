package com.example.solideapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.solideapp.databinding.ActivityHomepageBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Homepage extends AppCompatActivity implements ChangePasswordDialogFragment.ChangePasswordListener {

    ActivityHomepageBinding binding;
    private static final int REQUEST_ENABLE_BLUETOOTH = 2;
    BluetoothConnection bluetoothConnection;
    ApiManager apiManager;
    private int userId;

    private static boolean isNavigatedToOtherFragment = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomepageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new Beginscherm());

        Context context = this;
        bluetoothConnection = new BluetoothConnection(context);
        apiManager = new ApiManager(context);

        userId = getIntent().getIntExtra("userId", -1);
        Log.d("Homepage", "User ID: " + userId);
        Log.d("Homepage", "Navigated to other fragment: " + isNavigatedToOtherFragment);

        if (userId != -1) {
            Beginscherm beginschermFragment = new Beginscherm();
            Bundle args = new Bundle();
            args.putInt("userId", userId);
            beginschermFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, beginschermFragment)
                    .commit();

            fetchUserData(userId);
            bluetoothConnection.setUserId(userId);
        }

        binding.bottomNavigationView.setSelectedItemId(R.id.Beginscherm);

        binding.bottomNavigationView.setBackground(null);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Log.d("idForPage", String.valueOf(itemId));
            if (itemId == R.id.Beginscherm) {
                Intent homepageIntent = new Intent(this, Homepage.class);
                homepageIntent.putExtra("userId", userId);
                startActivity(homepageIntent);
                finish();
            } else if (itemId == R.id.bluetooth) {
                Bluetooth bluetoothFragment = new Bluetooth();
                Bundle args = new Bundle();
                args.putInt("userId", userId);
                bluetoothFragment.setArguments(args);
                replaceFragment(bluetoothFragment);
                isNavigatedToOtherFragment = true;
            } else if (itemId == R.id.profile) {
                Fragment profileFragment = new Profile();
                Bundle args = new Bundle();
                args.putInt("userId", userId);
                profileFragment.setArguments(args);
                replaceFragment(profileFragment);
                isNavigatedToOtherFragment = true;
            }
            return true;
        });

        if (!isNavigatedToOtherFragment) {
            BluetoothAdapter bluetoothAdapter = bluetoothConnection.getBluetoothAdapter();

            if (bluetoothAdapter == null) {
                Toast.makeText(this, "Bluetooth not supported on this device.", Toast.LENGTH_SHORT).show();
            }

            bluetoothConnection.requestBluetoothPermissions(this);

            bluetoothConnection.enableBluetooth(this);
            bluetoothConnection.scanForBLEDevices();
            bluetoothConnection.connectToDevice("FF:95:E8:75:38:3A");

            fetchSingleLocation();
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Bluetooth is now enabled", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                bluetoothConnection.enableBluetooth(this);
                Toast.makeText(getApplicationContext(), "Bluetooth is required for the app to function properly", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void fetchSingleLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    apiManager.getWeatherData(latitude, longitude, new ApiManager.WeatherApiResponseListener() {
                        @Override
                        public void onWeatherDataReceived(JSONObject response) {
                            try {
                                JSONObject mainObject = response.getJSONObject("main");
                                double tempKelvin = mainObject.getDouble("temp");
                                int tempCelsius = (int) Math.round(tempKelvin - 273.15);
                                String temperature = String.valueOf(tempCelsius) + "°";
                                Log.d("WeatherData", "Temperature is: " + temperature );

                                runOnUiThread(() -> {
                                    TextView temperatureTextView = findViewById(R.id.weatherTemperature);
                                    if (temperatureTextView != null) {
                                        temperatureTextView.setText(temperature);
                                    }
                                });

                                Log.d("WeatherData", String.valueOf(latitude));
                                Log.d("WeatherData", String.valueOf(longitude));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onWeatherDataError(String errorMessage) {
                            Log.e("WeatherData", "Error: " + errorMessage);
                        }
                    });

                    locationManager.removeUpdates(this);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(@NonNull String provider) {
                }

                @Override
                public void onProviderDisabled(@NonNull String provider) {
                }
            }, null);
        }
    }

    private void fetchUserData(int userId) {
        apiManager.getUserDataById(userId, new ApiManager.ApiResponseListener() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    JSONArray dataArray = response.getJSONArray("data");
                    if (dataArray.length() > 0) {
                        JSONObject userData = dataArray.getJSONObject(0);
                        String username = userData.getString("username");

                        runOnUiThread(() -> {
                            TextView welcomeTextName = findViewById(R.id.welcomeTextName);
                            if (welcomeTextName != null) {
                                welcomeTextName.setText(username);
                            }
                        });

                    } else {
                        Log.e("UserData", "No user data found in the response");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("UserData", "Error: " + errorMessage);
            }
        });
    }

    @Override
    public void onChangePassword(String newPassword, String newName) {
        // Implement the necessary code when the password changes in Homepage
        if (userId != -1) {
            initiatePasswordChange(getApplicationContext(), userId, newName, newPassword);
        }
    }

    private void initiatePasswordChange(Context context, int userId, String newPassword, String newName) {
        String passwordChangeApiUrl = "http://46.38.241.211:3000/api/user/change";
        ApiManager apiManager = new ApiManager(context);

        apiManager.changeUserDetails(context, userId, newPassword, newName, new ApiManager.ApiResponseListener() {
            @Override
            public void onSuccess(JSONObject response) {
                Toast.makeText(context, "Details zijn veranderd", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(context, "Failed to change details: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
