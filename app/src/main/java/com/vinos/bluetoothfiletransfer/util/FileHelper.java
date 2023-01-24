package com.vinos.bluetoothfiletransfer.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileHelper {

    public static boolean storeFile(File file, ByteArrayOutputStream fileOutputStream) {

        if (file == null || fileOutputStream == null) {
            return false;
        }

        try {
            FileOutputStream fos = new FileOutputStream(file);
            fileOutputStream.write(fileOutputStream.toByteArray());
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
