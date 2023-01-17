package com.vinos.bluetoothfiletransfer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vinos.bluetoothfiletransfer.bluetooth.BluetoothClient;
import com.vinos.bluetoothfiletransfer.bluetooth.BluetoothConnectionService;
import com.vinos.bluetoothfiletransfer.bluetooth.BluetoothServer;
import com.vinos.bluetoothfiletransfer.bluetooth.BluetoothServerController;
import com.vinos.bluetoothfiletransfer.bluetooth.Constant;
import com.vinos.bluetoothfiletransfer.bluetooth.CustomAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements CustomAdapter.DeviceSelectionListener {
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
        setContentView(R.layout.activity_main);
        adapter = BluetoothAdapter.getDefaultAdapter();
        buttonBluetooth = ((TextView) findViewById(R.id.turnOnTextView));
        deviceList = (RecyclerView) findViewById(R.id.deviceList);
        deviceList = (RecyclerView) findViewById(R.id.deviceList);
        selectFile = (TextView) findViewById(R.id.selectFile);
        sendButton = (TextView) findViewById(R.id.sendButton);
        receiveButton = (TextView) findViewById(R.id.receiveButton);
        deviceList.setLayoutManager(new LinearLayoutManager(this));
        checkPermissions();

        bluetoothConnectionService = new BluetoothConnectionService();
        buttonBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBluetooth();
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(selectedBluetoothDevice!=null && selectedFile!=null){
                    bluetoothConnectionService.startClient(selectedBluetoothDevice,selectedFile);
                }

            }
        });
        receiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                receiveButton();
            }
        });

        selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFile();
            }
        });

        if (!adapter.isEnabled()) {
            buttonBluetooth.setVisibility(View.VISIBLE);
        } else {
            buttonBluetooth.setVisibility(View.GONE);
        }
        getBondedDevices();
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "Select file"), 32343);
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
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @SuppressLint("MissingPermission")
    private void getBondedDevices(){
        devices = new ArrayList<>(adapter.getBondedDevices());
        CustomAdapter bluetoothDeviceArrayAdapter = new CustomAdapter(devices,this);
        deviceList.setAdapter(bluetoothDeviceArrayAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = data.getData();
        String fileAbsolutePath = RealPathUtil.getRealPath(this,uri);
        Log.v("ABC","Path : "+fileAbsolutePath);

        File file = new File(fileAbsolutePath);
        Log.v("ABC","Path : name: "+file.getName() +" "+" size: "+file.length());

    }

    private void checkPermissions() {
        String[] permissions = {android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.BLUETOOTH_CONNECT
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

        }else{
            buttonBluetooth.setVisibility(View.GONE);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onDeviceSelected(BluetoothDevice bluetoothDevice) {
        this.selectedBluetoothDevice = bluetoothDevice;
        Toast.makeText(this,bluetoothDevice.getName(),Toast.LENGTH_SHORT).show();
    }
}