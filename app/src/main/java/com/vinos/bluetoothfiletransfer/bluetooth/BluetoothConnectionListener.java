package com.vinos.bluetoothfiletransfer.bluetooth;

public interface BluetoothConnectionListener {
    void onConnected();

    boolean isConnected();

    void onConnectionFailure(String message);
}

