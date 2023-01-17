package com.vinos.bluetoothfiletransfer.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class BluetoothServer extends Thread {

    private String TAG = "BluetoothServer";
    BluetoothSocket socket;
    InputStream inputStream;
    OutputStream outputStream;

    public BluetoothServer(BluetoothSocket socket) {
        this.socket = socket;
        try {
            this.inputStream = this.socket.getInputStream();
            this.outputStream = this.socket.getOutputStream();
            Log.v(TAG,"BluetoothServer : inputStream,outputStream initialized");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        super.run();
        Log.v(TAG,"BluetoothServer : in run");
        try {
            if (true) {
                int totalFileNameSizeInBytes = 0;
                int totalFileSizeInBytes = 0;

                // File name string size
                byte[] fileNameSizeBuffer = new byte[4]; // Only 4 bytes needed for this operation, int => 4 bytes
                inputStream.read(fileNameSizeBuffer, 0, 4);
                ByteBuffer fileSizeBuffer = ByteBuffer.wrap(fileNameSizeBuffer);
                totalFileNameSizeInBytes = fileSizeBuffer.getInt();

                // String of file name
                byte[] fileNamebuffer = new byte[1024];
                inputStream.read(fileNamebuffer, 0, totalFileNameSizeInBytes);
                String fileName = new String(fileNamebuffer, 0, totalFileNameSizeInBytes);

                // File size integer bytes
                byte[] fileSizebuffer = new byte[4]; // int => 4 bytes
                inputStream.read(fileSizebuffer, 0, 4);
                fileSizeBuffer = ByteBuffer.wrap(fileSizebuffer);
                totalFileSizeInBytes = fileSizeBuffer.getInt();

                // The actual file bytes
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int read = -1;
                int totalBytesRead = 0;
                read = inputStream.read(buffer, 0, buffer.length);
                while (read != -1) {
                    baos.write(buffer, 0, read);
                    totalBytesRead += read;
                    if (totalBytesRead == totalFileSizeInBytes) {
                        break;
                    }
                    read = inputStream.read(buffer, 0, buffer.length);
                }
                baos.flush();


                FileOutputStream fos = new FileOutputStream("saveFile.path");
                fos.write(baos.toByteArray());
                fos.close();
            }
            sleep(5000);
            inputStream.close();
            outputStream.close();
            socket.close();
        } catch (Exception e) {
            Log.v(TAG,"BluetoothServer :"+e.getMessage());
            e.printStackTrace();
        }

    }
}
