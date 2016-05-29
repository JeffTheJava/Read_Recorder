package com.litmantech.readrecorder.utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Jeff_Dev_PC on 5/23/2016.
 */
public class UiUtil {
    /**
     * UI Utilities
     */

    /**
     * use this to save a string as a file on the HDD of the phone. The files does not have to be a .txt. it can be anything.
     *
     * @param file the file you want to save the string into will over write the file is exist.
     * @param data the message you want to save.
     * @throws IOException
     */
    public static void SaveStringToFile(File file, String data) throws IOException {
        FileOutputStream outputStream;
        outputStream = new FileOutputStream(file);
        outputStream.write(data.getBytes());
        outputStream.close();
    }
}
