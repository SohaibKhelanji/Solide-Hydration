package com.example.solideapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChangePasswordDialogFragment extends DialogFragment {
    Profile profileFragment = new Profile();
    int userId = -1;
    // Add an interface to communicate with the hosting fragment or activity
    public interface ChangePasswordListener {
        void onChangePassword(String newName, String newPassword);
    }

    private ChangePasswordListener changePasswordListener;

    public static ChangePasswordDialogFragment newInstance() {
        return new ChangePasswordDialogFragment();
    }

    // Add a setter method for the listener
    public void setChangePasswordListener(ChangePasswordListener listener) {
        this.changePasswordListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        EditText newName = view.findViewById(R.id.newName);
        EditText newPassword = view.findViewById(R.id.newPassword);
        Button confirmButton = view.findViewById(R.id.confirmButton);

        confirmButton.setOnClickListener(v -> onConfirmButtonClick(
                newName.getText().toString(),
                newPassword.getText().toString()));

        return view;
    }

    public void onConfirmButtonClick(String newName, String newPassword) {
        // Call the method to send the new password and new username to the API
        if (changePasswordListener != null) {
            changePasswordListener.onChangePassword(newName, newPassword);
        }

        // Dismiss the dialog when done
        dismiss();

    }


    // Set the listener when attaching the dialog to a fragment or activity
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            // This makes sure that the host activity or fragment has implemented the callback interface
            changePasswordListener = (ChangePasswordListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ChangePasswordListener");
        }
    }

}
