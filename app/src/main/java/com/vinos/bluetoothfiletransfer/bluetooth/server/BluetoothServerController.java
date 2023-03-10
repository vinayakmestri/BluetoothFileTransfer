package com.vinos.bluetoothfiletransfer.bluetooth.server;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.vinos.bluetoothfiletransfer.bluetooth.listeners.UpdateListener;
import com.vinos.bluetoothfiletransfer.util.Constant;

import java.io.IOException;

public class BluetoothServerController extends Thread {
    private String TAG = "BluetoothServerController";

    BluetoothServerSocket bluetoothServerSocket;
    boolean isCanceled = false;
    private UpdateListener updateListener;

    @SuppressLint("MissingPermission")
    public BluetoothServerController(UpdateListener updateListener) {
        this.updateListener = updateListener;
        init();
    }

    private void init() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter != null) {
                Log.v(TAG, "BluetoothAdapter : initialized");
                bluetoothServerSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("Bluetooth", Constant.uuid);
                isCanceled = false;
            } else {
                Log.v(TAG, "BluetoothAdapter : null");
                isCanceled = true;
                bluetoothServerSocket = null;
                updateListener.onConnectionFailure("BluetoothAdapter not initialized");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        BluetoothSocket socket = null;
        Log.v(TAG,"In run method");
        while (true) {
            Log.v(TAG,"while");
            if (this.isCanceled) {
                break;
            }

            try {
                socket = bluetoothServerSocket.accept();
            } catch (IOException e) {
                Log.v(TAG,"IOException : " + e.getMessage());
                break;
            }

            if (!this.isCanceled && socket != null) {
                Log.v(TAG, "BluetoothServer : starting");
                updateListener.onConnected();
                BluetoothServer bluetoothServer = new BluetoothServer(socket);
                bluetoothServer.setUpdateListener(updateListener);
                bluetoothServer.start();
            }
        }
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public void reset() {
        isCanceled = false;
        init();
    }
}
