package com.vinos.bluetoothfiletransfer.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.UUID;

public class BluetoothClient extends Thread {

    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket socket;
    private File file;
    @SuppressLint("MissingPermission")
    public BluetoothClient(BluetoothDevice bluetoothDevice, File file) {
        this.file = file;
        this.bluetoothDevice = bluetoothDevice;
        try {
            this.socket = bluetoothDevice.createRfcommSocketToServiceRecord(Constant.uuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void run() {
        super.run();

        try {
            this.socket.connect();
        } catch (IOException e) {
            return;
        }

        try {

            OutputStream outputStream = this.socket.getOutputStream();
            InputStream inputStream = this.socket.getInputStream();
            byte[] fileBytes = null;
            try {
                fileBytes = new byte[(int) file.length()];
                BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
                bufferedInputStream.read(fileBytes, 0, fileBytes.length);
                bufferedInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteBuffer fileNameSize = ByteBuffer.allocate(4);
            fileNameSize.putInt(file.getName().getBytes().length);

            ByteBuffer fileSize = ByteBuffer.allocate(4);
            fileSize.putInt(fileBytes.length);

            outputStream.write(fileNameSize.array());
            outputStream.write(file.getName().getBytes());
            outputStream.write(fileSize.array());
            outputStream.write(fileBytes);

             Constant.isConnectionSuccess = true;

            sleep(5000);
            outputStream.close();
            inputStream.close();
            this.socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
