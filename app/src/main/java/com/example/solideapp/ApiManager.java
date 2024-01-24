package com.example.solideapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ApiManager {

    private static final String BASE_URL = "http://46.38.241.211:3000";
    private static final String API_KEY = "250b26c6f3f18b9b0f588c82d19d233f";
    private static final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";
    private RequestQueue requestQueue;


    public ApiManager(Context context) {
        // Instantiate the RequestQueue using Volley
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }


    //LOGIN METHOD
    public void loginUser(String email, String password, final ApiResponseListener listener) {
        String loginUrl = BASE_URL + "/api/user/login?";
        Uri.Builder builder = Uri.parse(loginUrl).buildUpon();
        builder.appendQueryParameter("email", email);
        builder.appendQueryParameter("password", password);
        String finalLoginUrl = builder.build().toString();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, finalLoginUrl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle successful registration response
                        listener.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle registration errors
                        listener.onError(error.getMessage());
                    }
                });

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    //REGISTER METHOD
    public void registerUser(String username, String email, String password, final ApiResponseListener listener) {
        String registerUrl = BASE_URL + "/api/user/register?";
        Uri.Builder builder = Uri.parse(registerUrl).buildUpon();
        builder.appendQueryParameter("email", email);
        builder.appendQueryParameter("username", username);
        builder.appendQueryParameter("password", password);
        String finalRegisterUrl = builder.build().toString();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, finalRegisterUrl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle successful registration response
                        listener.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle registration errors
                        Log.e("VolleyError", error.toString()); // Log the Volley error message
                        listener.onError(error.getMessage());
                    }
                });

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    // GET USER DATA BY ID METHOD
    public void getUserDataById(int userId, final ApiResponseListener listener) {
        String getUserUrl = BASE_URL + "/api/user/get?id=" + userId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, getUserUrl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle successful user data response
                        listener.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle errors
                        Log.e("VolleyError", error.toString()); // Log the Volley error message
                        listener.onError(error.getMessage());
                    }
                });

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    // Update user's daily goal method
    public void updateUserGoal(int userId, String newGoal, final ApiResponseListener listener) {
        String updateGoalUrl = BASE_URL + "/api/goal/change?id=" + userId + "&newGoal=" + newGoal;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.PUT, updateGoalUrl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle successful response
                        listener.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle errors
                        Log.e("VolleyError", error.toString()); // Log the Volley error message
                        listener.onError(error.getMessage());
                    }
                });

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    public void getUserGoal(int userId, final ApiResponseListener listener) {
        String getUserGoalUrl = BASE_URL + "/api/goal/get?id=" + userId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, getUserGoalUrl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle successful response
                        listener.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle errors
                        Log.e("VolleyError", error.toString()); // Log the Volley error message
                        listener.onError(error.getMessage());
                    }
                });

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    // Delete a user's bottle by ID method
    public void deleteUserBottle(int bottleId, final ApiResponseListener listener) {
        String deleteBottleUrl = BASE_URL + "/api/bottle/delete?id=" + bottleId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.DELETE, deleteBottleUrl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle successful bottle deletion response
                        listener.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle bottle deletion errors
                        listener.onError(error.getMessage());
                    }
                });

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    public void addWaterDataToBottle(int userId, int waterAmount, final ApiResponseListener listener) {
        // Specify the base URL for adding water data
        String baseUrl = "http://46.38.241.211:3000/api/waterdata/drink";

        // Use Uri.Builder to add parameters to the URL
        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
        builder.appendQueryParameter("id", String.valueOf(userId));
        builder.appendQueryParameter("water", String.valueOf(waterAmount));

        // Get the complete URL
        String apiUrl = builder.build().toString();

        // Create a JsonObjectRequest for a POST request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                apiUrl,
                null,  // No request body for a POST request with parameters in the URL
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Invoke the onSuccess callback with the response
                        listener.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Invoke the onError callback with the error message
                        listener.onError(error.getMessage());
                    }
                });

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    public void getUserWaterIntakeData(int userId, final ApiResponseListener listener) {
        String getUserWaterIntakeUrl = BASE_URL + "/api/waterdata/getdrink?id=" + userId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, getUserWaterIntakeUrl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle successful response
                        listener.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle errors
                        Log.e("VolleyError", error.toString()); // Log the Volley error message
                        listener.onError(error.getMessage());
                    }
                });

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }


    public interface ApiResponseListener {
        void onSuccess(JSONObject response);

        void onError(String errorMessage);
    }

    // Get bottles associated with a user method
    public void getUserBottles(int userId, final UserBottlesApiResponseListener listener) {
        String getUserBottlesUrl = BASE_URL + "/api/bottle/getalluser?user_id=" + userId;
        Log.d("GetAllBottlesUrl", "getUserBottles: " + getUserBottlesUrl);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, getUserBottlesUrl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Check if the response contains the "data" key
                            if (response.has("data")) {
                                // Extract the array of bottles
                                JSONArray bottlesArray = response.getJSONArray("data");

                                // Check if the user has one or more bottles
                                if (bottlesArray.length() > 0) {
                                    // User has bottles, return the data
                                    listener.onUserBottlesReceived(bottlesArray);
                                } else {
                                    // User has no bottles
                                    listener.onUserBottlesEmpty();
                                }
                            } else {
                                // Handle missing "data" key in the response
                                listener.onUserBottlesError("Invalid response format");
                            }
                        } catch (JSONException e) {
                            // Handle JSON parsing error
                            listener.onUserBottlesError(e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle errors
                        listener.onUserBottlesError(error.getMessage());
                    }
                });

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }


    public interface UserBottlesApiResponseListener {
        void onUserBottlesReceived(JSONArray bottles);

        void onUserBottlesEmpty();

        void onUserBottlesError(String errorMessage);
    }


    // Create a bottle for a user method
    public void createBottleForUser(int userId, int weight, final ApiResponseListener listener) {
        // Check if the user already has a bottle
        getUserBottles(userId, new UserBottlesApiResponseListener() {
            @Override
            public void onUserBottlesReceived(JSONArray bottles) {
                // Check if the user already has a bottle
                if (bottles.length() > 0) {
                    try {
                        // Extract the bottle ID from the first bottle in the array
                        JSONObject firstBottle = bottles.getJSONObject(0);
                        int bottleId = firstBottle.getInt("id");

                        // Log that the user already has a bottle
                        Log.d("BottleAlreadyExists", "User already has a bottle with ID: " + bottleId);

                        // Delete the existing bottle
                        deleteUserBottle(bottleId, new ApiResponseListener() {
                            @Override
                            public void onSuccess(JSONObject response) {
                                // Successfully deleted the existing bottle
                                Log.d("BottleDeletion", "Deleted existing bottle with ID: " + bottleId);

                                // Proceed to create a new bottle
                                createNewBottle(userId, weight, listener);
                            }

                            @Override
                            public void onError(String errorMessage) {
                                // Handle errors in bottle deletion
                                Log.e("BottleDeletionError", errorMessage);
                                listener.onError(errorMessage); // Forward the error to the original listener
                            }
                        });
                    } catch (JSONException e) {
                        // Handle JSON parsing error
                        listener.onError(e.getMessage());
                    }
                } else {
                    // User doesn't have a bottle, proceed to create a new one
                    createNewBottle(userId, weight, listener);
                }
            }

            @Override
            public void onUserBottlesEmpty() {
                // User doesn't have a bottle, proceed to create a new one
                createNewBottle(userId, weight, listener);
            }

            @Override
            public void onUserBottlesError(String errorMessage) {
                // Handle errors when checking user bottles
                listener.onError(errorMessage);
            }
        });
    }

    private void createNewBottle(int userId, int weight, ApiResponseListener listener) {
        String bottleName = "Bottle " + weight + "g";
        String createBottleUrl = BASE_URL + "/api/bottle/create?weight=" + weight + "&name=" + bottleName + "&user_id=" + userId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, createBottleUrl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle successful bottle creation response
                        listener.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle bottle creation errors
                        listener.onError(error.getMessage());
                    }
                });

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }


    public void getWeatherData(double latitude, double longitude, final WeatherApiResponseListener listener) {
        String url = WEATHER_URL + "?lat=" + latitude + "&lon=" + longitude + "&appid=" + API_KEY;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle successful response
                        listener.onWeatherDataReceived(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle errors
                        listener.onWeatherDataError(error.getMessage());
                    }
                });

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    public interface WeatherApiResponseListener {
        void onWeatherDataReceived(JSONObject response);

        void onWeatherDataError(String errorMessage);
    }

    public boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

    public void changeUserDetails(Context context, int userId, String newPassword, String newName, ApiResponseListener listener) {
        // Specify the base URL
        String baseUrl = "http://46.38.241.211:3000/api/user/change";

        // Use Uri.Builder to add parameters to the URL
        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
        builder.appendQueryParameter("id", String.valueOf(userId));

        // Include the new username if it's not empty
        if (!newName.isEmpty()) {
            builder.appendQueryParameter("newname", newName);
        }

        // Include the new password if it's not empty
        if (!newPassword.isEmpty()) {
            builder.appendQueryParameter("newpassword", newPassword);
        }

        // Get the complete URL
        String apiUrl = builder.build().toString();

        // Create a new RequestQueue using the provided context
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        // Create a JsonObjectRequest for a PUT request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.PUT,
                apiUrl,
                null,  // No request body for a PUT request with parameters in the URL
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Invoke the onSuccess callback with the response
                        listener.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Invoke the onError callback with the error message
                        listener.onError(error.getMessage());
                    }
                });

        // Add the request to the Volley RequestQueue
        requestQueue.add(jsonObjectRequest);
    }
}

