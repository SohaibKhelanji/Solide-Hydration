package com.example.solideapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.solideapp.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class Beginscherm extends Fragment {

    ApiManager apiManager;
    private int userId; // Variable to store the user ID


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_beginscherm, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            userId = getArguments().getInt("userId", -1);
            Log.d("BEGINSCHERM", "User ID in Fragment: " + userId); // Add this log statement
        }

        apiManager = new ApiManager(requireContext());

        getUserGoalAndUpdateUI();
        getUserWaterIntakeForCurrentDay();
        ImageView dailyGoalImageView = view.findViewById(R.id.dailyGoal);


        if (dailyGoalImageView != null) {
            dailyGoalImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Call the method when the ImageView is clicked
                    showDailyGoalInputDialog();
                }
            });
        }

    }




    private void showDailyGoalInputDialog() {
        // Get the layout inflater
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_input, null);

        // Create an AlertDialog.Builder and set the view
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        // Set the title
        builder.setTitle("Enter Daily Goal");

        // Set the positive button
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the entered value from the EditText
                EditText editText = dialogView.findViewById(R.id.editTextDailyGoal);
                String input = editText.getText().toString();

                // Validate the input (you can add your own validation logic here)
                if (!input.isEmpty()) {
                    // Call the APIManager method to update the user's goal
                    updateDailyGoal(input);
                } else {
                    // Display a toast for empty input or invalid input
                    Toast.makeText(requireContext(), "Invalid input", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set the negative button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cancelled by the user
                dialog.cancel();
            }
        });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Method to update the user's daily goal using the APIManager
    private void updateDailyGoal(String newGoal) {
        // Validate that userId is not -1 (invalid)
        if (userId != -1) {
            // Call the APIManager method to update the user's goal
            apiManager.updateUserGoal(userId, newGoal, new ApiManager.ApiResponseListener() {
                @Override
                public void onSuccess(JSONObject response) {
                    // Handle successful response (if needed)
                    getUserGoalAndUpdateUI();
                    Toast.makeText(requireContext(), "Daily goal updated successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String errorMessage) {
                    // Log the error message for debugging
                    Log.e("Beginscherm", "Error updating daily goal: " + errorMessage);

                    // Handle error (if needed)
                    Toast.makeText(requireContext(), "Error updating daily goal: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Log an error or handle the case where userId is invalid
            Log.e("Beginscherm", "Invalid userId");
            Toast.makeText(requireContext(), "Invalid user ID", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to get the user's goal and update the UI
    private void getUserGoalAndUpdateUI() {
        apiManager.getUserGoal(userId, new ApiManager.ApiResponseListener() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    // Extract the goal value from the JSON response
                    int userGoal = response.getInt("data");

                    // Update the TextView with the user's goal
                    TextView usersGoalTextView = getView().findViewById(R.id.usersGoal);
                    if (usersGoalTextView != null) {
                        usersGoalTextView.setText(String.valueOf(userGoal));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Log the error message for debugging
            }
        });
    }

    private void getUserWaterIntakeForCurrentDay() {
        // Validate that userId is not -1 (invalid)
        if (userId != -1) {
            // Get the current date in the format "yyyy-MM-dd"
            String currentDate = getCurrentDate();

            // Call the APIManager method to get user's water intake data
            apiManager.getUserWaterIntakeData(userId, new ApiManager.ApiResponseListener() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        // Check if the response contains the "data" key
                        if (response.has("data")) {
                            // Extract the array of water intake entries
                            JSONArray waterIntakeArray = response.getJSONArray("data");

                            // Initialize a variable to store the total water intake
                            int totalWaterIntake = 0;

                            // Loop through each entry in the array
                            for (int i = 0; i < waterIntakeArray.length(); i++) {
                                // Get the current entry
                                JSONObject entry = waterIntakeArray.getJSONObject(i);

                                // Extract the "created_at" value from the entry
                                String createdAt = entry.getString("created_at");

                                // Check if the "created_at" date matches the current date
                                if (createdAt.startsWith(currentDate)) {
                                    // Extract the "water_intake" value from the entry and add it to the total
                                    int waterIntake = entry.getInt("water_intake");
                                    totalWaterIntake += waterIntake;
                                }
                            }

                            // Log or use the totalWaterIntake as needed
                            Log.d("Beginscherm", "Total water intake for the current day: " + totalWaterIntake);
                            updateCurrentHydrationTextView(totalWaterIntake);
                        } else {
                            // Handle missing "data" key in the response
                            Log.e("Beginscherm", "Invalid response format");
                        }
                    } catch (JSONException e) {
                        // Handle JSON parsing error
                        Log.e("Beginscherm", "Error parsing JSON: " + e.getMessage());
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    // Log the error message for debugging
                    Log.e("Beginscherm", "Error getting user's water intake: " + errorMessage);
                }
            });
        } else {
            // Log an error or handle the case where userId is invalid
            Log.e("Beginscherm", "Invalid userId");
        }
    }

    // Method to get the current date in the format "yyyy-MM-dd"
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Method to update the TextView with the total water intake for the current day
    private void updateCurrentHydrationTextView(int totalWaterIntake) {
        // Find the TextView with id "currentHydration"
        View fragmentView = getView();

        if (fragmentView != null) {
            TextView currentHydrationTextView = fragmentView.findViewById(R.id.currentHydration);

            // Update the TextView with the total water intake value
            if (currentHydrationTextView != null) {
                currentHydrationTextView.setText(String.valueOf(totalWaterIntake));
            }
        }
    }
}
