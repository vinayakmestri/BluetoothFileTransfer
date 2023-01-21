package com.vinos.bluetoothfiletransfer.bluetooth;

import static com.vinos.bluetoothfiletransfer.bluetooth.Constant.isConnectionSuccess;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;

public class BluetoothConnectionService extends Service {

    BluetoothConnectionListener bluetoothConnectionListener = new BluetoothConnectionListener() {
        @Override
        public void onConnected() {
            updateListener.onConnected();
        }

        @Override
        public boolean isConnected() {
            return false;
        }

        @Override
        public void onConnectionFailure(String message) {
            if (message != null)
                updateListener.onConnectionFailure(message);
        }
    };

    IBinder binder = new BluetoothBinder();
    private String TAG = "BluetoothConnectionService";

    BluetoothServerController bluetoothServerController;
    UpdateListener updateListener = new UpdateListener() {
        @Override
        public void onConnected() {
            Log.v(TAG, "BluetoothServer : onConnected ");
        }

        @Override
        public boolean isConnected() {
            return false;
        }

        @Override
        public void onConnectionFailure(String message) {
            Log.v(TAG, "BluetoothServer : error :" + message);
        }

        @Override
        public void onProgressChanged(int progress) {

            Log.v(TAG, "BluetoothServer : file transfer progress " + progress);

        }

        @Override
        public void onFileFinished() {
            Log.v(TAG, "BluetoothServer : File transfer successful");
        }
    };
    FileProgressListener fileProgressListener = new FileProgressListener() {
        @Override
        public void onProgressChanged(int progress) {
            updateListener.onProgressChanged(progress);
        }

        @Override
        public void onFileFinished() {
            updateListener.onFileFinished();
        }
    };

    public void startServer() {
        bluetoothServerController = new BluetoothServerController(bluetoothConnectionListener, fileProgressListener);
        bluetoothServerController.start();
    }

    public Boolean startClient(BluetoothDevice device, File file) {
        BluetoothClient bluetoothClient = new BluetoothClient(device, file);
        bluetoothClient.setBluetoothConnectionListener(bluetoothConnectionListener);
        bluetoothClient.setFileProgressListener(fileProgressListener);
        bluetoothClient.start();
        try {
            bluetoothClient.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return isConnectionSuccess;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setUpdateListener(UpdateListener updateListener) {
        this.updateListener = updateListener;
    }

    public class BluetoothBinder extends Binder {
        public BluetoothConnectionService getService() {
            // Return this instance of LocalService so clients can call public methods
            return BluetoothConnectionService.this;
        }
    }

    public void registerListener(UpdateListener updateListener) {
        this.updateListener = updateListener;
    }
}
