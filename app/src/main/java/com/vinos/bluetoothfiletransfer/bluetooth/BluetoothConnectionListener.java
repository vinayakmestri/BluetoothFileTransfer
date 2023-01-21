package com.vinos.bluetoothfiletransfer.bluetooth;

public interface BluetoothConnectionListener {

    void onStarted();

    void onConnected();

    void onFinished();

    boolean isConnected();

    void onConnectionFailure(String message);
}

