package com.example.solideapp;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class BluetoothConnection {

    public static final int REQUEST_BLUETOOTH_PERMISSION = 1;
    private static final int REQUEST_ENABLE_BLUETOOTH = 2;

    private final Context context;
    Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean scanning;
    private static final long SCAN_PERIOD = 10000; // 10 seconds
    private Handler handler = new Handler();
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCallback gattCallback;
    private static final String TAG = BluetoothConnection.class.getSimpleName();
    private static final UUID SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID RECEIVE_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    private BluetoothGattCharacteristic receiveCharacteristic;

    private static final UUID SEND_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    private int userId;  // Variable to store the user ID


    public BluetoothConnection(Context context) {
        this.context = context;
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        }
    }

    // Method to set the user ID
    public void setUserId(int userId) {
        this.userId = userId;
    }

    // Method to get the user ID
    public int getUserId() {
        return userId;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public void requestBluetoothPermissions(Activity activity) {
        // Define an array containing Bluetooth-related permissions
        String[] permissions = {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE
        };

        // Create a list to store permissions that need to be requested
        List<String> permissionsToRequest = new ArrayList<>();

        // Check each permission in the array if it's already granted
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Add permissions that are not granted to the list
                permissionsToRequest.add(permission);
            }
        }

        // If there are permissions to request, initiate the permission request
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    activity,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_BLUETOOTH_PERMISSION
            );
        } else {
            Log.d("BluetoothPermissions", "No Bluetooth permissions need to be checked.");
        }
    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public void enableBluetooth(final Activity activity) {
        if (!isBluetoothEnabled()) {
            // Bluetooth is not enabled, prompt the user to enable it
            activity.startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH);
        }
    }

    public void scanForBLEDevices() {
        if (!scanning) {
            // Stops scanning after a predefined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScan();
                    if (!scanning) {
                        Log.d("BLE_Device", "Stopped scanning or No devices found.");
                    }
                }
            }, SCAN_PERIOD);

            scanning = true;
            startScan();
        } else {
            stopScan();
        }
    }

    private void startScan() {
        if (bluetoothLeScanner != null) {
            bluetoothLeScanner.startScan(leScanCallback);
        }
    }

    private void stopScan() {
        if (bluetoothLeScanner != null && scanning) {
            bluetoothLeScanner.stopScan(leScanCallback);
            scanning = false;
        }
    }

    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            Log.d("BLE_Device", "Name: " + device.getName() + ", Address: " + device.getAddress());
        }
    };

    // Method to connect to a BLE device using its MAC address
    // Micro:Bit MAC Address = FF:95:E8:75:38:3A
    public boolean connectToDevice(String deviceAddress) {
        if (bluetoothAdapter == null || deviceAddress == null) {
            Log.w("BluetoothConnection", "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        final CountDownLatch connectionLatch = new CountDownLatch(2); // Countdown latch to wait for the connection

        try {
            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            gattCallback = new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    Log.d("bluetoothDebuggingCheck", "Connection State Change: " + newState);
                    if (newState == BluetoothGatt.STATE_CONNECTED) {
                        // Connected to the GATT Server
                        Log.d("BluetoothConnection", "Connected to GATT server.");
                        connectionLatch.countDown(); // Countdown latch on successful connection
                        bluetoothGatt.discoverServices();
                    } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                        // Disconnected from the GATT Server
                        Log.d("BluetoothConnection", "Disconnected from GATT server.");
                        connectionLatch.countDown(); // Countdown latch on disconnection
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    Log.d("bluetoothDebuggingCheck", "Services Discovered Status: " + status);
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        BluetoothGattService service = gatt.getService(SERVICE_UUID);
                        if (service != null) {
                            receiveCharacteristic = service.getCharacteristic(RECEIVE_UUID);
                            if (receiveCharacteristic != null) {
                                gatt.setCharacteristicNotification(receiveCharacteristic, true);

                                // Enable indications for the receive characteristic by writing the descriptor
                                BluetoothGattDescriptor descriptor = receiveCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE); // Use ENABLE_INDICATION_VALUE
                                gatt.writeDescriptor(descriptor);
                            }
                        }
                    } else {
                        Log.w(TAG, "onServicesDiscovered received: " + status);
                    }
                }

                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    // Log when characteristic data changes
                    Log.d("bluetoothDebuggingCheck", "Characteristic Changed");
                    byte[] data = characteristic.getValue();
                    if (data != null && data.length > 0) {
                        StringBuilder stringBuilder = new StringBuilder(data.length);
                        for (byte byteChar : data) {
                            stringBuilder.append(String.format("%02X ", byteChar));
                        }
                        String receivedData = new String(data); // Convert byte array to string
                        Log.d("microBit", receivedData);
                        if (receivedData.contains("weight:")) {
                            String numericPart = extractNumericPart(receivedData);
                            if (numericPart != null) {
                                Log.d("microBit", "Extracted weight: " + numericPart);

                                // Assuming you have a reference to the ApiManager instance
                                ApiManager apiManager = new ApiManager(context);

                                // Replace USER_ID with the actual user ID
                                int userId = getUserId(); // Assuming you have a method to get the user ID
                                Log.d("CreateBottle", "Creating bottle for User ID: " + userId);
                                int weight = Integer.parseInt(numericPart);

                                // Call the method to create a bottle for the user
                                apiManager.createBottleForUser(userId, weight, new ApiManager.ApiResponseListener() {
                                    @Override
                                    public void onSuccess(JSONObject response) {
                                        // Handle success if needed
                                        Log.d("microBit", "Bottle created successfully");
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        // Handle error if needed
                                        Log.e("microBit", "Error creating bottle: " + errorMessage);
                                    }
                                });
                            }
                        } else if (receivedData.contains("drink:")) {
                            Log.d("microBit", "Inside drink data block");
                            String numericPart = extractNumericPart(receivedData);
                            if (numericPart != null) {
                                Log.d("microBit", "Extracted drink value: " + numericPart);
                                int userId = getUserId(); // Replace with the actual method to get the user ID
                                int waterAmount = Integer.parseInt(numericPart);

                                ApiManager apiManager = new ApiManager(context);
                                apiManager.addWaterDataToBottle(userId, waterAmount, new ApiManager.ApiResponseListener() {
                                    @Override
                                    public void onSuccess(JSONObject response) {
                                        // Handle success if needed
                                        Log.d("microBit", "Water data added to the bottle successfully");

                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        // Handle error if needed
                                        Log.e("microBit", "Error adding water data to the bottle: " + errorMessage);
                                    }
                                });
                            }
                        } else {
                            // Do something else when the target sentence is not found
                            Log.d(TAG, "Received data does not contain the target sentence.");
                        }
                    } else {
                        Log.d("bluetoothDebuggingCheck", "Received empty data");
                    }
                }
                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    // This method will be called after a read operation, if implemented
                }

                // ... (other callback methods as needed)
            };

            bluetoothGatt = device.connectGatt(context, false, gattCallback);


            // Wait for the connection state to change
            boolean isConnected = connectionLatch.await(1, TimeUnit.SECONDS); // Adjust timeout as needed

            return isConnected;
        } catch (IllegalArgumentException | InterruptedException exception) {
            Log.w("BluetoothConnection", "Device not found with provided address or connection interrupted. Unable to connect.");
            return false;
        }

    }

    private String extractNumericPart(String input) {
        String pattern = "(drink|weight):(\\d+)";
        java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher matcher = regex.matcher(input);

        if (matcher.find()) {
            return matcher.group(2); // Group 2 contains the numeric part
        } else {
            return null; // Pattern not found
        }
    }
}
