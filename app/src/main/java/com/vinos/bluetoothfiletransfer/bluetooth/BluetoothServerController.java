package com.vinos.bluetoothfiletransfer.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;

public class BluetoothServerController extends Thread {
    private String TAG = "BluetoothServerController";

    BluetoothServerSocket bluetoothServerSocket;
    boolean isCanceled = false;

    @SuppressLint("MissingPermission")
    public BluetoothServerController() {
        init();
    }

    private void init() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter != null) {
                Log.v(TAG, "BluetoothAdapter : initialized");
                bluetoothServerSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("BluetoothFileTransfer", Constant.uuid);
                isCanceled = false;
            } else {
                Log.v(TAG, "BluetoothAdapter : null");
                isCanceled = true;
                bluetoothServerSocket = null;
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
                new BluetoothServer(socket).start();
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
