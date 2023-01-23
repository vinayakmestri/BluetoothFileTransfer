package com.vinos.bluetoothfiletransfer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.vinos.bluetoothfiletransfer.adapter.BluetoothDeviceAdapter;
import com.vinos.bluetoothfiletransfer.bluetooth.BluetoothConnectionService;
import com.vinos.bluetoothfiletransfer.bluetooth.listeners.UpdateListener;
import com.vinos.bluetoothfiletransfer.util.RealPathUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BluetoothDeviceAdapter.DeviceSelectionListener {

    boolean mBound = false;
    public static int BLUETOOTH_DEVICE_SELECT_CODE = 4354345;
    BluetoothAdapter adapter;
    List<BluetoothDevice> devices;
    int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 3433;
    int MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT = 8757;
    TextView buttonBluetooth, sendButton, receiveButton, selectFile, selectDevice, progressTextView;
    LinearProgressIndicator linearProgressIndicator;
    RecyclerView deviceList;
    BluetoothConnectionService bluetoothConnectionService;
    BluetoothDevice selectedBluetoothDevice;
    File selectedFile;
    LinearLayout mainController;
    private String TAG = "MainActivity";
    UpdateListener updateListener = new UpdateListener() {
        @Override
        public void onStarted() {
            showProgress(0);
        }

        @Override
        public void onConnected() {

        }

        @Override
        public void onFinished() {
            showProgress(0);
        }

        @Override
        public boolean isConnected() {
            return false;
        }

        @Override
        public void onConnectionFailure(String message) {

        }

        @Override
        public void onProgressChanged(int value) {
            Log.v(TAG, "MainActivity showProgress:" + value);
            showProgress(value);
        }

        @Override
        public void onFileFinished() {
            showProgress(0);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "File transfer successfully", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private void showProgress(int value) {

        Log.v(TAG, "showProgress" + value);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                linearProgressIndicator.setProgressCompat(value, true);
                progressTextView.setText(value + "%");
            }
        });


    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.v(TAG, "onServiceConnected");
            BluetoothConnectionService.BluetoothBinder binder = (BluetoothConnectionService.BluetoothBinder) service;
            bluetoothConnectionService = binder.getService();
            bluetoothConnectionService.registerListener(updateListener);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.v(TAG, "onServiceDisconnected");
            mBound = false;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        refreshUI();
    }

    private void refreshUI() {
        if (adapter == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Alert").setMessage("Bluetooth is not supported")
                    .setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).show();
        } else if (!adapter.isEnabled()) {
            buttonBluetooth.setVisibility(View.VISIBLE);
            mainController.setVisibility(View.GONE);
        } else {
            buttonBluetooth.setVisibility(View.GONE);
            mainController.setVisibility(View.VISIBLE);
            Intent intent = new Intent(MainActivity.this, BluetoothConnectionService.class);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "Select file"), 32343);
    }


    private void receiveButton() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {

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
        BluetoothDeviceAdapter bluetoothDeviceArrayAdapter = new BluetoothDeviceAdapter(devices, this);
        deviceList.setAdapter(bluetoothDeviceArrayAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BLUETOOTH_DEVICE_SELECT_CODE && resultCode == RESULT_OK) {
            if (data.hasExtra("device")) {
                selectedBluetoothDevice = (BluetoothDevice) data.getExtras().get("device");
            }
            selectDevice.setText("Device :" + selectedBluetoothDevice.getName());
            return;
        } else if (resultCode == RESULT_OK) {
            try {
                Uri uri = data.getData();
                String fileAbsolutePath = RealPathUtil.getRealPath(this, uri);
                Log.v("ABC", "Path : " + fileAbsolutePath);
                selectFile.setText("File :" + fileAbsolutePath);
                File file = new File(fileAbsolutePath);
                selectedFile = file;
                Log.v("ABC", "Path : name: " + file.getName() + " " + " size: " + file.length());
            } catch (Exception e) {
                Toast.makeText(this, "Select image file", Toast.LENGTH_SHORT).show();
            }


        }

    }

    private String getPermission() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return android.Manifest.permission.BLUETOOTH_CONNECT;
        } else {
            return android.Manifest.permission.BLUETOOTH_ADMIN;
        }
    }

    private boolean checkPermissions() {
        String[] permissions = {getPermission(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {

                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    ActivityCompat.requestPermissions(
                            this, permissions,
                            MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT
                    );
                }

                Log.v(TAG, permission + "is not given");
                return false;
            }
        }
        return true;

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v("onDestroy", "onDestroy");
        if (serviceConnection != null) {
            Log.v("onDestroy", "unbindService");
            unbindService(serviceConnection);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adapter = BluetoothAdapter.getDefaultAdapter();
        buttonBluetooth = ((TextView) findViewById(R.id.turnOnTextView));
        selectFile = (TextView) findViewById(R.id.selectFile);
        sendButton = (TextView) findViewById(R.id.sendButton);
        selectDevice = (TextView) findViewById(R.id.selectDevice);
        receiveButton = (TextView) findViewById(R.id.receiveButton);
        mainController = (LinearLayout) findViewById(R.id.mainController);
        linearProgressIndicator = (LinearProgressIndicator) findViewById(R.id.progress);
        progressTextView = findViewById(R.id.progressTextView);
        linearProgressIndicator.setMax(100);
        linearProgressIndicator.setProgress(0);
        //deviceList.setLayoutManager(new LinearLayoutManager(this));
        checkPermissions();

        bluetoothConnectionService = new BluetoothConnectionService();
        buttonBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkPermissions())
                    startBluetooth();
            }
        });
        selectDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermissions()) {
                    Intent intent = new Intent(MainActivity.this, DeviceListActivity.class);
                    startActivityForResult(intent, BLUETOOTH_DEVICE_SELECT_CODE);
                }
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissions()) {
                    if (selectedBluetoothDevice != null && selectedFile != null) {
                        bluetoothConnectionService.startClient(selectedBluetoothDevice, selectedFile);
                    } else {
                        Toast.makeText(MainActivity.this, "Select file and device first", Toast.LENGTH_SHORT).show();
                    }
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

    }
}