package com.example.fiscalprintertest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.datecs.fiscalprinter.SDK.model.DatecsFiscalDevice;

import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdConfig;
import com.example.fiscalprintertest.connectivity.BluetoothSppConnector;
import com.example.fiscalprintertest.databinding.ActivityMainBinding;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private BluetoothSocket socket = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        // UI adjustments
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obtain a reference to the system Bluetooth adapter
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }

        // Bluetooth permissions check
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 1);
        } else {

            // App has bluetooth permissions, showing device list
            Toast.makeText(this, "Device list", Toast.LENGTH_SHORT).show();
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                StringBuilder builder = new StringBuilder();

                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    builder.append(deviceName);
                    builder.append("\n");
                    builder.append(deviceHardwareAddress);
                    builder.append("\n\n");

                }
                // setting the text with devices to the TextView
                binding.pairedDevicesTv.setText(builder.toString());

            } else {
                Toast.makeText(this, "No paired devices", Toast.LENGTH_SHORT).show();
            }

            // Used for multithreading
            ExecutorService executorService = Executors.newFixedThreadPool(3);

            binding.selectButton.setOnClickListener(v -> {
                String deviceAddress = binding.devAddrEt.getText().toString();
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
                if (device == null) {
                    Toast.makeText(this, "Device not found", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(this, "Device found", Toast.LENGTH_SHORT).show();
                    int state = device.getBondState();

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Bond state: " + state, Toast.LENGTH_SHORT).show();
                    });

                    try {
                        socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));

                        executorService.execute(() -> {
                            try {
                                socket.connect();
                                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                                writer.println("Hello from Android");
                                writer.flush();

                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            });

            binding.sendButton.setOnClickListener(v -> {
                String message = binding.messageEt.getText().toString();
                if (!message.isEmpty() && socket != null){
                    PrintWriter writer;
                    try {
                        writer = new PrintWriter(socket.getOutputStream(), true);
                        writer.println(message);
                        writer.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // let the user know what would happen depending on the option they chose
    }
}