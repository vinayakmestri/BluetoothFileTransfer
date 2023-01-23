package com.vinos.bluetoothfiletransfer.bluetooth.client;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.vinos.bluetoothfiletransfer.bluetooth.listeners.UpdateListener;
import com.vinos.bluetoothfiletransfer.util.Constant;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class BluetoothClient extends Thread {

    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket socket;
    private File file;

    private UpdateListener updateListener;


    @SuppressLint("MissingPermission")
    public BluetoothClient(BluetoothDevice bluetoothDevice, File file, UpdateListener updateListener) {
        this.file = file;
        this.bluetoothDevice = bluetoothDevice;
        this.updateListener = updateListener;
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
            if (this.updateListener != null) {
                this.updateListener.onConnectionFailure(e.getMessage());
            }
            return;
        }

        try {

            OutputStream outputStream = this.socket.getOutputStream();
            InputStream inputStream = this.socket.getInputStream();
            byte[] fileBytes = null;
            try {
                if (this.updateListener != null) {
                    this.updateListener.onStarted();
                }
                fileBytes = new byte[(int) file.length()];
                BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
                bufferedInputStream.read(fileBytes, 0, fileBytes.length);
                bufferedInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                if (this.updateListener != null) {
                    this.updateListener.onConnectionFailure(e.getMessage());
                }
            }

            ByteBuffer fileNameSize = ByteBuffer.allocate(4);
            fileNameSize.putInt(file.getName().getBytes().length);

            ByteBuffer fileSize = ByteBuffer.allocate(4);
            fileSize.putInt(fileBytes.length);

            Log.i("File name size :", "" + file.getName().getBytes().length);
            Log.i("File size :", "" + fileBytes.length);

            outputStream.write(fileNameSize.array());
            outputStream.write(file.getName().getBytes());
            outputStream.write(fileSize.array());
            outputStream.write(fileBytes);

           /* BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(fileBytes));
            byte[] readBuffer = new byte[1024];
            int count = 0;
            int total = 0;
            while ((count = in.read(readBuffer, 0, readBuffer.length)) != -1) {
                total += count;
                outputStream.write(readBuffer, 0, count);
                int progress = (int) ((total * 100) / fileBytes.length);
                sleep(100);
                this.updateProgress(progress);
            }*/

            Constant.isConnectionSuccess = true;

            sleep(5000);
            outputStream.close();
            inputStream.close();
            this.socket.close();

            if (this.updateListener != null) {
                this.updateListener.onFileFinished();
                this.updateListener.onFinished();
            }

        } catch (Exception e) {
            if (this.updateListener != null) {
                this.updateListener.onConnectionFailure(e.getMessage());
            }
            e.printStackTrace();
        }
    }

    private void updateProgress(int progress) {
        if (this.updateListener != null && progress % 5 == 0) {
            Log.i("Client Upload progress change", "" + progress);
            updateListener.onProgressChanged(progress);
        }
    }
}
