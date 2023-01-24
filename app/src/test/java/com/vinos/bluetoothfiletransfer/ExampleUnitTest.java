package com.vinos.bluetoothfiletransfer;

import static org.junit.Assert.assertEquals;

import com.vinos.bluetoothfiletransfer.util.FileHelper;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {


    @Test
    public void test_wrongPath_File_Store() {
        ByteArrayOutputStream boas = new ByteArrayOutputStream(45);
        boolean testNull = FileHelper.storeFile(new File("/storage/emulated/0/file1.txt"), boas);
        assertEquals(false, testNull);
    }

    @Test
    public void test_null_File_Store() {
        boolean testNull = FileHelper.storeFile(null, null);
        assertEquals(false, testNull);
    }
}