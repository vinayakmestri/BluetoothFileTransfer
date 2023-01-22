package com.vinos.bluetoothfiletransfer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vinos.bluetoothfiletransfer.adapter.CustomAdapter;
import com.vinos.bluetoothfiletransfer.bluetooth.BluetoothConnectionService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DeviceListActivity extends AppCompatActivity implements CustomAdapter.DeviceSelectionListener {
    BluetoothAdapter adapter;
    List<BluetoothDevice> devices;
    int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 3433;
    int MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT = 8757;
    TextView buttonBluetooth, sendButton, receiveButton, selectFile;
    RecyclerView deviceList;
    BluetoothConnectionService bluetoothConnectionService;
    BluetoothDevice selectedBluetoothDevice;
    File selectedFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        adapter = BluetoothAdapter.getDefaultAdapter();
        buttonBluetooth = ((TextView) findViewById(R.id.turnOnTextView));
        deviceList = (RecyclerView) findViewById(R.id.deviceList);
        deviceList.setLayoutManager(new LinearLayoutManager(this));
        checkPermissions();

        bluetoothConnectionService = new BluetoothConnectionService();
        buttonBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBluetooth();
            }
        });


        if (!adapter.isEnabled()) {
            buttonBluetooth.setVisibility(View.VISIBLE);
        } else {
            buttonBluetooth.setVisibility(View.GONE);
        }
        getBondedDevices();
    }


    private void receiveButton() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        try {
            bluetoothConnectionService.startServer();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @SuppressLint("MissingPermission")
    private void getBondedDevices() {
        devices = new ArrayList<>(adapter.getBondedDevices());
        CustomAdapter bluetoothDeviceArrayAdapter = new CustomAdapter(devices, this);
        deviceList.setAdapter(bluetoothDeviceArrayAdapter);
    }


    private void checkPermissions() {
        String[] permissions = {Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
            )
            ) {
                ActivityCompat.requestPermissions(
                        this, permissions,
                        MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT
                );
            }
            return;
        }

    }

    private void startBluetooth() {

        if (!adapter.isEnabled()) {
            buttonBluetooth.setVisibility(View.GONE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            adapter.enable();

        } else {
            buttonBluetooth.setVisibility(View.GONE);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onDeviceSelected(BluetoothDevice bluetoothDevice) {
        this.selectedBluetoothDevice = bluetoothDevice;
        Toast.makeText(this, bluetoothDevice.getName(), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent();
        intent.putExtra("device", bluetoothDevice);
        setResult(RESULT_OK, intent);
        finish();
    }
}