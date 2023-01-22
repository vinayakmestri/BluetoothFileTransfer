package com.vinos.bluetoothfiletransfer.bluetooth.client;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.vinos.bluetoothfiletransfer.bluetooth.listeners.UpdateListener;
import com.vinos.bluetoothfiletransfer.util.Constant;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
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
    public BluetoothClient(BluetoothDevice bluetoothDevice, File file) {
        this.file = file;
        this.bluetoothDevice = bluetoothDevice;

        if (updateListener != null) {
            if (bluetoothDevice == null) {
                updateListener.onConnectionFailure("Device is not selected");
                return;
            }
            if (file == null) {
                updateListener.onConnectionFailure("File is not selected");
                return;
            }
        }

        try {
            this.socket = bluetoothDevice.createRfcommSocketToServiceRecord(Constant.uuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setUpdateListener(UpdateListener updateListener) {
        this.updateListener = updateListener;
    }


    @SuppressLint("MissingPermission")
    @Override
    public void run() {
        super.run();

        try {
            this.socket.connect();
        } catch (IOException e) {
            if (updateListener != null) {
                updateListener.onConnectionFailure(e.getMessage());
            }
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
                if (updateListener != null) {
                    updateListener.onConnectionFailure(e.getMessage());
                }
            }

            ByteBuffer fileNameSize = ByteBuffer.allocate(4);
            fileNameSize.putInt(file.getName().getBytes().length);

            ByteBuffer fileSize = ByteBuffer.allocate(4);
            fileSize.putInt(fileBytes.length);

            outputStream.write(fileNameSize.array());
            outputStream.write(file.getName().getBytes());
            outputStream.write(fileSize.array());
            //outputStream.write(fileBytes);

            BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(fileBytes));
            byte[] readBuffer = new byte[2048];
            int count = 0;
            int total = 0;
            while ((count = in.read(readBuffer, 0, readBuffer.length)) != -1) {
                total += count;
                outputStream.write(readBuffer, 0, readBuffer.length);
                Log.i("TOTAL", Long.toString(total));
                int progress = (int) ((total * 100) / fileBytes.length);
                Log.i("Upload progress", "" + progress);
                updateProgress(progress);
            }

            Constant.isConnectionSuccess = true;

            sleep(9000);
            outputStream.close();
            inputStream.close();
            this.socket.close();
            if (updateListener != null) {
                updateListener.onFileFinished();
            }
        } catch (Exception e) {
            if (updateListener != null) {
                updateListener.onConnectionFailure(e.getMessage());
            }
            e.printStackTrace();
        }
    }

    private void updateProgress(int progress) {
        if (updateListener != null) {
            updateListener.onProgressChanged(progress);
        }
    }
}
