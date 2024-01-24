package com.example.solideapp;

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Bluetooth extends Fragment {

    private TextView bottleConnectedTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        // Find the bottleConnected TextView
        bottleConnectedTextView = rootView.findViewById(R.id.bottleConnected);

        // Get the user ID from the arguments
        int userId = getArguments().getInt("userId", -1);

        if (userId != -1) {
            // Call the function to get user's bottle and update the bottleConnected TextView
            getUserBottleAndSetTextView(userId);
        } else {
            // Handle the case where the user ID is not available
            Log.e("BluetoothFragment", "User ID not found");
        }

        return rootView;
    }

    // Function to get user's bottle (if any) and update the bottleConnected TextView
    private void getUserBottleAndSetTextView(int userId) {
        ApiManager apiManager = new ApiManager(requireContext()); // Use requireContext() for the context

        // Use the getUserBottles method from ApiManager
        apiManager.getUserBottles(userId, new ApiManager.UserBottlesApiResponseListener() {
            @Override
            public void onUserBottlesReceived(JSONArray bottles) {
                try {
                    // Check if the response contains the "data" key and has at least one bottle
                    if (bottles != null && bottles.length() > 0) {
                        // Get the first bottle in the array (assuming each user has at most one bottle)
                        JSONObject userBottle = bottles.getJSONObject(0);

                        // Extract the name of the bottle
                        String bottleName = userBottle.getString("name");

                        // Update the bottleConnected TextView with the bottle name
                        updateBottleConnectedTextView(bottleName);
                        bottleConnectedTextView.setTextColor(Color.parseColor("#006400"));
                    } else {
                        // User has no bottles, update TextView accordingly
                        updateBottleConnectedTextView("No bottle");
                    }
                } catch (JSONException e) {
                    // Handle JSON parsing error
                    Log.e("BottleName", "Error parsing JSON: " + e.getMessage());
                }
            }

            @Override
            public void onUserBottlesEmpty() {
                // User has no bottles, update TextView accordingly
                updateBottleConnectedTextView("No bottle");
            }

            @Override
            public void onUserBottlesError(String errorMessage) {
                // Handle errors when getting user bottles, update TextView accordingly
                updateBottleConnectedTextView("Error getting bottles");
                Log.e("BottleName", "Error getting user bottles: " + errorMessage);
            }
        });
    }

    // Helper function to update the bottleConnected TextView
    private void updateBottleConnectedTextView(String bottleName) {
        if (bottleConnectedTextView != null) {
            bottleConnectedTextView.setText(bottleName);
        }
    }
}
