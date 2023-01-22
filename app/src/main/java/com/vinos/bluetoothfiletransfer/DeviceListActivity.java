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

import com.vinos.bluetoothfiletransfer.adapter.BluetoothDeviceAdapter;

import java.util.ArrayList;
import java.util.List;

public class DeviceListActivity extends AppCompatActivity implements BluetoothDeviceAdapter.DeviceSelectionListener {
    BluetoothAdapter adapter;
    List<BluetoothDevice> devices;
    int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 3433;
    int MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT = 8757;
    TextView buttonBluetooth, sendButton, receiveButton, selectFile;
    RecyclerView deviceList;
    BluetoothDevice selectedBluetoothDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        adapter = BluetoothAdapter.getDefaultAdapter();
        buttonBluetooth = ((TextView) findViewById(R.id.turnOnTextView));
        deviceList = (RecyclerView) findViewById(R.id.deviceList);
        deviceList.setLayoutManager(new LinearLayoutManager(this));
        checkPermissions();

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


    @SuppressLint("MissingPermission")
    private void getBondedDevices() {
        devices = new ArrayList<>(adapter.getBondedDevices());
        BluetoothDeviceAdapter bluetoothDeviceArrayAdapter = new BluetoothDeviceAdapter(devices, this);
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
        Toast.makeText(this, bluetoothDevice.getName(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.putExtra("device", bluetoothDevice);
        setResult(RESULT_OK, intent);
        finish();
    }
}