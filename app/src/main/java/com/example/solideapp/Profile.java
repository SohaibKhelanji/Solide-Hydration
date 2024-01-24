package com.example.solideapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class Profile extends Fragment implements ChangePasswordDialogFragment.ChangePasswordListener {
    private TextView userName;
    private int userId;
    private Switch switch2;
    public static final int YOUR_PERMISSION_REQUEST_CODE = 123;
    private static final String NOTIFICATION_CHANNEL_ID = "your_channel_id";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        TextView changePasswordButton = view.findViewById(R.id.changePasswordButton);
        switch2 = view.findViewById(R.id.switch2);
        userName = view.findViewById(R.id.userName);

        // Get userId from arguments
        userId = getArguments().getInt("userId", -1);

        TextView logoutButton = view.findViewById(R.id.logoutButton);

        logoutButton.setOnClickListener(v -> {
            // Restart the app by relaunching the Login activity
            Intent intent = new Intent(requireContext(), Loginscherm.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        int userId = getArguments().getInt("userId", -1);
        if (userId != -1) {
            fetchUserData(userId);
        }

        TextView aboutUsButton = view.findViewById(R.id.textView6);
        aboutUsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAboutUsDialog();
            }
        });

        Button probutton = view.findViewById(R.id.button);
        probutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                probutton();
            }
        });

        // Notifications
        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("SwitchStateChanged", "Switch state changed. isChecked: " + isChecked);
                if (isChecked) {
                    Toast.makeText(requireContext(), "Notificaties aan", Toast.LENGTH_SHORT).show();
                    checkAndRequestNotificationPermission();
                } else {
                    // Handle the case where the switch is turned off
                    switch2.setChecked(false); // This line ensures that the switch state is always off if the user tries to turn it off
                    Toast.makeText(requireContext(), "Notificaties uit", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Change password button click listener
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the method to show the change password dialog
                showChangePasswordDialog();
            }
        });

        return view;
    }

    public void fetchUserData(int userId) {
        ApiManager apiManager = new ApiManager(requireContext());

        apiManager.getUserDataById(userId, new ApiManager.ApiResponseListener() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    // Extract username from the API response
                    String username = response.getJSONArray("data").getJSONObject(0).getString("username");

                    // Update the userName TextView with the retrieved username
                    requireActivity().runOnUiThread(() -> userName.setText(username));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Handle errors
            }
        });
    }

    // Modify this method to be triggered only when the button is clicked
    private void showChangePasswordDialog() {
        if (isAdded()) {
            ChangePasswordDialogFragment dialogFragment = ChangePasswordDialogFragment.newInstance();
            dialogFragment.setChangePasswordListener(this);
            dialogFragment.show(getChildFragmentManager(), "ChangePasswordDialogFragment");
        }
    }

    @Override
    public void onChangePassword(String newPassword, String newUsername) {
        // Call the method to initiate details change with the new password and username
        if (userId != -1) {
            initiatePasswordChange(requireContext(), userId, newUsername, newPassword);
        }
    }

    private void initiatePasswordChange(Context context, int userId, String newPassword, String newName) {
        // Replace the following URL with your actual password change endpoint
        String passwordChangeApiUrl = "http://46.38.241.211:3000/api/user/change";

        ApiManager apiManager = new ApiManager(context);

        apiManager.changeUserDetails(requireContext(), userId, "newPassword", "newUsername", new ApiManager.ApiResponseListener() {
            @Override
            public void onSuccess(JSONObject response) {
                // Handle the success response
                Toast.makeText(requireContext(), "Details zijn veranderd", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                // Handle errors
                Toast.makeText(requireContext(), "Failed to change details: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Your Channel Name";
            String description = "Your Channel Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification() {
        // Check for notification permissions
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_NOTIFICATION_POLICY) == PackageManager.PERMISSION_GRANTED) {
            // Create an intent to launch when the user taps the notification
            Intent intent = new Intent(requireContext(), MainActivity.class); // Replace with your main activity
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


            // Create a notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Solide Notificatie")
                    .setContentText("Je ontvangt nu Solide Notificaties")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            // Show the notification
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        } else {
            // Handle the case where notification permissions are not granted
            Toast.makeText(requireContext(), "Notification permissions not granted", Toast.LENGTH_SHORT).show();
        }
    }

    public void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create a notification channel if not already created (required for Android Oreo and above)
            createNotificationChannel();
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_NOTIFICATION_POLICY) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_NOTIFICATION_POLICY}, YOUR_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, you can proceed with sending notifications
            sendNotification();
        }
    }

    private void showAboutUsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("About Us");
        builder.setMessage("Wij zijn Solide Inc." + "Wij zijn Zaahir, Sohaib, Anthony, Sheldon en Maria." + "Wij hebben een Solide Sensor gemaakt die je waterdata inhoud."
        );

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void probutton() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Solide PRO");
        builder.setMessage("Solide PRO is nog niet beschikbaar."
        );

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
