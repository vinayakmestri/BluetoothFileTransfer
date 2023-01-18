package com.vinos.bluetoothfiletransfer.bluetooth;

public interface FileProgressListener {
    void onProgressChanged(int progress);

    void onFileFinished();

}