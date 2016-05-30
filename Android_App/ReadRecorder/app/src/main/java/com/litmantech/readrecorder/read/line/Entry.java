package com.litmantech.readrecorder.read.line;

import android.content.Context;

import com.litmantech.readrecorder.read.Session;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Jeff_Dev_PC on 5/20/2016.
 */
public class Entry {

    private static final String TAG = "Entry";
    private static Context context;
    private final String sentence;
    private static FileOutputStream outputStream;
    private final int uniqueID;
    private final File audioFile;
    private Session session;

    public Entry(String sentence, int uniqueID, File sessionDir) {
        this.sentence = sentence;
        this.uniqueID = uniqueID;

        String audioFileName = uniqueID + ".raw";
        audioFile = new File(sessionDir,audioFileName );

    }

    /**
     * Get the sentence that this entry was created based off of.
     * @return
     */
    public String getSentence(){
        return sentence;
    }


    /**
     * Steam in the audio data you want to save to the HDD of the device.  you can save a little then wait then save more then wait. dont need to do it all at once.
     * Must call close() when you are done streaming in audio data.
     * @see #close()
     * @param audioBuff the raw audio data you want to save
     */
    public void SaveAudio(short[] audioBuff) {
        try {

            ByteBuffer byteBuffer = ByteBuffer.allocate(audioBuff.length * 2);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            byteBuffer.asShortBuffer().put(audioBuff);

            if(outputStream == null)
                outputStream = new FileOutputStream(audioFile);
            outputStream.write(byteBuffer.array());

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void close() {
        try {
            outputStream.flush();
            outputStream.close();
            outputStream=null;
            session.MakeDirVisibleOverUSB(audioFile.getAbsolutePath());
        } catch (IOException e) {

        }
    }


    public void setSession(Session session) {
        this.session = session;
    }
}
