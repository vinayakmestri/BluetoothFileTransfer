package com.vinos.bluetoothfiletransfer.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

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

    private FileProgressListener fileProgressListener;
    private BluetoothConnectionListener bluetoothConnectionListener;


    @SuppressLint("MissingPermission")
    public BluetoothClient(BluetoothDevice bluetoothDevice, File file) {
        this.file = file;
        this.bluetoothDevice = bluetoothDevice;

        if (bluetoothConnectionListener != null) {
            if (bluetoothDevice == null) {
                bluetoothConnectionListener.onConnectionFailure("Device is not selected");
                return;
            }
            if (file == null) {
                bluetoothConnectionListener.onConnectionFailure("File is not selected");
                return;
            }
        }

        try {
            this.socket = bluetoothDevice.createRfcommSocketToServiceRecord(Constant.uuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setBluetoothConnectionListener(BluetoothConnectionListener bluetoothConnectionListener) {
        this.bluetoothConnectionListener = bluetoothConnectionListener;
    }

    public void setFileProgressListener(FileProgressListener fileProgressListener) {
        this.fileProgressListener = fileProgressListener;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void run() {
        super.run();

        try {
            this.socket.connect();
        } catch (IOException e) {
            if (bluetoothConnectionListener != null) {
                bluetoothConnectionListener.onConnectionFailure(e.getMessage());
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
                if (bluetoothConnectionListener != null) {
                    bluetoothConnectionListener.onConnectionFailure(e.getMessage());
                }
            }

            ByteBuffer fileNameSize = ByteBuffer.allocate(4);
            fileNameSize.putInt(file.getName().getBytes().length);

            ByteBuffer fileSize = ByteBuffer.allocate(4);
            fileSize.putInt(fileBytes.length);

            outputStream.write(fileNameSize.array());
            outputStream.write(file.getName().getBytes());
            outputStream.write(fileSize.array());
            outputStream.write(fileBytes);

            BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(fileBytes));
            byte[] readBuffer = new byte[1024];
            int count = 0;
            int total = 0;
            while ((count = in.read(readBuffer, 0, readBuffer.length)) != -1) {
                total += count;
                outputStream.write(readBuffer, 0, readBuffer.length);
                Log.i("TOTAL", Long.toString(total));
                int progress = (int) ((total * 100) / fileBytes.length);
                Log.i("Upload progress", "" + progress);
                if (fileProgressListener != null) {
                    fileProgressListener.onProgressChanged(progress);
                }
            }

            Constant.isConnectionSuccess = true;

            sleep(5000);
            outputStream.close();
            inputStream.close();
            this.socket.close();
            if (fileProgressListener != null) {
                fileProgressListener.onFileFinished();
            }
        } catch (Exception e) {
            if (bluetoothConnectionListener != null) {
                bluetoothConnectionListener.onConnectionFailure(e.getMessage());
            }
            e.printStackTrace();
        }
    }
}
