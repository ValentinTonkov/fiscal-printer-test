package com.example.fiscalprintertest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

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

                } else {
                    BluetoothSppConnector connector = new BluetoothSppConnector(device);
                    executorService.execute(() -> {
                        try {
                            connector.connect();
                            PrinterManager.getInstance().init(connector);
                            DatecsFiscalDevice fiscalDevice = PrinterManager.getFiscalDevice();

                            String modelVendorName = PrinterManager.instance.getModelVendorName();

                            runOnUiThread(() -> {

                                if (fiscalDevice == null){
                                    Toast.makeText(MainActivity.this, "Cannot connect", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (fiscalDevice.isConnectedPrinter()){
                                    Toast.makeText(this, "Printer is connected", Toast.LENGTH_SHORT).show();
                                }
                                Toast.makeText(MainActivity.this, modelVendorName, Toast.LENGTH_SHORT).show();
                            });

                            // here we can test the commands

                            // set date and time
                            cmdConfig.DateTime myClock= new cmdConfig.DateTime();
                            myClock.setDateTime("29-09-24","12:00:00");

                            Thread.sleep(500);
                            cmdConfig.DateTime clock = new cmdConfig.DateTime();

                            runOnUiThread(() -> {
                                try {
                                    Toast.makeText(this, clock.getDateTime(), Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            });



                        } catch (Exception e) {
                            e.printStackTrace();


                        }
                    });

                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // let the user know what would happen depending on the option they chose
    }
}