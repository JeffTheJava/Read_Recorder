package com.litmantech.readrecorder.utilities;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Jeff_Dev_PC on 5/23/2016.
 */
public class UiUtil {
    private static final String TAG = "UiUtil";
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

    /**
     *
     * @param txtDoc
     * @return
     * @throws IOException if this reader is closed or some other I/O error occurs.
     * @throws FileNotFoundException will throw if file file does not exists
     */
    public static String[] OpenTxtDocLineByLine(File txtDoc) throws IOException{
        if(!txtDoc.exists())
            throw new FileNotFoundException("File "+txtDoc.getName()+" does not exists");

        ArrayList<String> newLineTextDoc = new ArrayList<String>();

        FileInputStream is = null;

        is = new FileInputStream(txtDoc);

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line = reader.readLine();
        while(line != null){
            Log.d(TAG, line);
            line = line.trim();//clean it up
            if(!line.isEmpty())
                newLineTextDoc.add(line);
            line = reader.readLine();
        }
        return newLineTextDoc.toArray(new String[0]);//Originally this code used new String[list.size()]. However, i found a blogpost revealing that due to JVM optimizations, using new String[0] is better now. http://shipilev.net/blog/2016/arrays-wisdom-ancients/
    }
}
