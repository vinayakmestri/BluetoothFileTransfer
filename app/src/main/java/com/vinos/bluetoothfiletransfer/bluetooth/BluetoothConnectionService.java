package com.vinos.bluetoothfiletransfer.bluetooth;

import static com.vinos.bluetoothfiletransfer.bluetooth.Constant.isConnectionSuccess;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.io.File;

public class BluetoothConnectionService extends Service {

    IBinder binder = new BluetoothBinder();

    UpdateListener updateListener;

    BluetoothServerController bluetoothServerController;
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
            updateListener.onConnectionFailure(message);
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
}
