package com.vinos.bluetoothfiletransfer.bluetooth.server;

import android.bluetooth.BluetoothSocket;
import android.os.Environment;
import android.util.Log;

import com.vinos.bluetoothfiletransfer.bluetooth.listeners.FileProgressListener;
import com.vinos.bluetoothfiletransfer.bluetooth.listeners.UpdateListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
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

    private FileProgressListener fileProgressListener;
    private UpdateListener updateListener;


    public void setUpdateListener(UpdateListener updateListener) {
        this.updateListener = updateListener;
    }


    public BluetoothServer(BluetoothSocket socket) {
        this.socket = socket;
        try {
            this.inputStream = this.socket.getInputStream();
            this.outputStream = this.socket.getOutputStream();
            Log.v(TAG, "BluetoothServer : inputStream,outputStream initialized");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        super.run();
        Log.v(TAG, "BluetoothServer : in run");
        try {
            if (true) {
                if (updateListener != null) {
                    updateListener.onStarted();
                }
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

                Log.i("File name size :", "" + totalFileNameSizeInBytes);
                Log.i("File size :", "" + totalFileSizeInBytes);

                // The actual file bytes
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int read = 0;
                int totalBytesRead = 0;
                read = inputStream.read(buffer, 0, buffer.length);
                int count = 0;
                while (read != -1) {
                    baos.write(buffer, 0, read);
                    totalBytesRead += read;

                    int progress = (int) ((totalBytesRead * 100) / totalFileSizeInBytes);
                    Log.i("Upload progress", "" + progress);
                    if (updateListener != null) {
                        updateListener.onProgressChanged(progress);
                    }

                    if (totalBytesRead == totalFileSizeInBytes) {
                        break;
                    }
                    Log.v(TAG, "BluetoothServer : read:" + totalBytesRead + " out of " + totalFileSizeInBytes);
                    read = inputStream.read(buffer, 0, buffer.length);
                }
                baos.flush();

                File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                Log.v(TAG, "BluetoothServer : file  path :" + directory.getAbsolutePath());
                File file = new File(directory, String.valueOf(fileName));
                Log.v(TAG, "BluetoothServer : file actual  path :" + file.getAbsolutePath());
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(baos.toByteArray());
                fos.close();

                //boolean isWriteSuccess = FileHelper.storeFile(file,baos);
                //Log.v(TAG, "BluetoothServer : file transfer successfully " + isWriteSuccess++);

                Log.v(TAG, "BluetoothServer : file transfer successfully " + count++);
            }
            sleep(5000);
        } catch (Exception e) {
            if (updateListener != null) {
                updateListener.onConnectionFailure(e.getMessage());
            }
            Log.v(TAG, "BluetoothServer :" + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
                outputStream.close();
                socket.close();
                if (updateListener != null)
                    updateListener.onStarted();

                if (updateListener != null)
                    updateListener.onFileFinished();
            } catch (Exception e) {
                if (updateListener != null) {
                    updateListener.onConnectionFailure(e.getMessage());
                }
            }
        }
    }
}
