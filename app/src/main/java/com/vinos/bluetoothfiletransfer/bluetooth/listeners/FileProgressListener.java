package com.vinos.bluetoothfiletransfer.bluetooth.listeners;

public interface FileProgressListener {
    void onProgressChanged(int progress);

    void onFileFinished();

}