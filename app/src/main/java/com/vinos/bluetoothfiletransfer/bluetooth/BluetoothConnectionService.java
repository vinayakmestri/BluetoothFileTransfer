package com.vinos.bluetoothfiletransfer.bluetooth;

import static com.vinos.bluetoothfiletransfer.bluetooth.Constant.isConnectionSuccess;

import android.bluetooth.BluetoothDevice;

import java.io.File;

public class BluetoothConnectionService {

    BluetoothServerController bluetoothServerController;
    public void startServer() {
        bluetoothServerController = new BluetoothServerController();
        bluetoothServerController.start();
    }

    public Boolean startClient(BluetoothDevice device, File file) {
        BluetoothClient bluetoothClient = new BluetoothClient(device,file);
        bluetoothClient.start();
        try {
            bluetoothClient.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return isConnectionSuccess;
    }
}
